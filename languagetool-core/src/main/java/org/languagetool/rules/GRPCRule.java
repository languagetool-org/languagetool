/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Streams;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.ml.MLServerGrpc;
import org.languagetool.rules.ml.MLServerProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base class fur rules running on external servers;
 * see gRPC service definition in languagetool-core/src/main/proto/ml_server.proto
 *
 * @see #create(ResourceBundle, RemoteRuleConfig, String, String, Map)  for an easy to add rules; return rule in Language::getRelevantRemoteRules
 * add it like this:
  <pre>
   public List&lt;Rule&gt; getRelevantRemoteRules(ResourceBundle messageBundle, List&lt;RemoteRuleConfig&gt; configs, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List&lt;Language&gt; altLanguages) throws IOException {
     List&lt;Rule&gt; rules = new ArrayList&lt;&gt;(super.getRelevantRemoteRules(
     messageBundle, configs, globalConfig, userConfig, motherTongue, altLanguages));
     Rule exampleRule = GRPCRule.create(messageBundle,
       RemoteRuleConfig.getRelevantConfig("EXAMPLE_ID", configs),
      "EXAMPLE_ID", "example_rule_id",
      Collections.singletonMap("example_match_id", "example_rule_message"));
     rules.add(exampleRule);
     return rules;
   }
  </pre>

 */
public abstract class GRPCRule extends RemoteRule {
  private static final Logger logger = LoggerFactory.getLogger(GRPCRule.class);

  public static String cleanID(String id) {
    return id.replaceAll("[^a-zA-Z_]", "_");
  }
  /**
   * Internal rule to create rule matches with IDs based on Match Sub-IDs
   */
  protected class GRPCSubRule extends Rule {
    private final String subId;
    private final String matchId;

    GRPCSubRule(String subId) {
      this.subId = subId;
      this.matchId = GRPCRule.this.getId() + "_" + cleanID(subId);
    }

    @Override
    public String getId() {
      return matchId;
    }

    @Override
    public String getDescription() {
      return GRPCRule.this.getDescription();
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new UnsupportedOperationException();
    }

  }

  static class Connection {
    final ManagedChannel channel;
    final MLServerGrpc.MLServerBlockingStub stub;

    private ManagedChannel getChannel(String host, int port, boolean useSSL,
                                      @Nullable String clientPrivateKey, @Nullable  String clientCertificate,
                                      @Nullable String rootCertificate) throws SSLException {
      NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port);
      if (useSSL) {
        SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();
        if (rootCertificate != null) {
          sslContextBuilder.trustManager(new File(rootCertificate));
        }
        if (clientCertificate != null && clientPrivateKey != null) {
          sslContextBuilder.keyManager(new File(clientCertificate), new File(clientPrivateKey));
        }
        channelBuilder = channelBuilder.negotiationType(NegotiationType.TLS).sslContext(sslContextBuilder.build());
      } else {
        channelBuilder = channelBuilder.usePlaintext();
      }
      return channelBuilder.build();
    }

    Connection(RemoteRuleConfig serviceConfiguration) throws SSLException {
      String host = serviceConfiguration.getUrl();
      int port = serviceConfiguration.getPort();
      boolean ssl = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("secure", "false"));
      String key = serviceConfiguration.getOptions().get("clientKey");
      String cert = serviceConfiguration.getOptions().get("clientCertificate");
      String ca = serviceConfiguration.getOptions().get("rootCertificate");
      this.channel = getChannel(host, port, ssl, key, cert, ca);
      this.stub = MLServerGrpc.newBlockingStub(channel);

    }

    private void shutdown() {
      if (channel != null) {
        channel.shutdownNow();
      }
    }
  }

  private static final LoadingCache<RemoteRuleConfig, Connection> servers =
    CacheBuilder.newBuilder().build(CacheLoader.from(serviceConfiguration -> {
      if (serviceConfiguration == null) {
        throw new IllegalArgumentException("No configuration for connection given");
      }
      try {
        return new Connection(serviceConfiguration);
      } catch (SSLException e) {
        throw new RuntimeException(e);
      }
    }));

  static {
    shutdownRoutines.add(() -> servers.asMap().values().forEach(Connection::shutdown));
  }

  private final Connection conn;

  public GRPCRule(ResourceBundle messages, RemoteRuleConfig config) {
    super(messages, config);

    synchronized (servers) {
      Connection conn = null;
        try {
          conn = servers.get(serviceConfiguration);
        } catch (Exception e) {
          logger.error("Could not connect to remote service at " + serviceConfiguration, e);
        }
      this.conn = conn;
    }
  }

  protected class MLRuleRequest extends RemoteRule.RemoteRequest {
    final MLServerProto.MatchRequest request;
    final List<AnalyzedSentence> sentences;

    public MLRuleRequest(MLServerProto.MatchRequest request, List<AnalyzedSentence> sentences) {
      this.request = request;
      this.sentences = sentences;
    }
  }

  @Override
  protected RemoteRule.RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, AnnotatedText annotatedText) {
    List<String> text = sentences.stream().map(AnalyzedSentence::getText).collect(Collectors.toList());
    MLServerProto.MatchRequest req = MLServerProto.MatchRequest.newBuilder().addAllSentences(text).build();
    return new MLRuleRequest(req, sentences);
  }

  @Override
  protected Callable<RemoteRuleResult> executeRequest(RemoteRule.RemoteRequest request) {
    return () -> {
      MLRuleRequest req = (MLRuleRequest) request;

      MLServerProto.MatchResponse response = conn.stub.match(req.request);
      Map<AnalyzedSentence, Integer> offsets = new HashMap<>();
      int offset = 0;
      for (int i = 0; i < req.sentences.size(); i++) {
        AnalyzedSentence sentence = req.sentences.get(i);
        offsets.put(sentence, offset);
        offset += sentence.getText().length();
      }
      List<RuleMatch> matches = Streams.zip(response.getSentenceMatchesList().stream(), req.sentences.stream(), (matchList, sentence) ->
        matchList.getMatchesList().stream().map(match -> {
            int relativeOffset = offsets.get(sentence);
            GRPCSubRule subRule = new GRPCSubRule(match.getSubId());
            RuleMatch m = new RuleMatch(subRule, sentence,
              relativeOffset + match.getOffset(),
              relativeOffset + match.getOffset() + match.getLength(),
              getMessage(match, sentence));
            m.setSuggestedReplacements(match.getSuggestionsList());
            return m;
          }
        )
      ).flatMap(Function.identity()).collect(Collectors.toList());
      RemoteRuleResult result = new RemoteRuleResult(true, matches);
      return result;
    };
  }

  protected abstract String getMessage(MLServerProto.Match match, AnalyzedSentence sentence);

  @Override
  protected RemoteRuleResult fallbackResults(RemoteRule.RemoteRequest request) {
    return new RemoteRuleResult(false, Collections.emptyList());
  }

  /**
   * Helper method to create instances of RemoteMLRule
   * @param messages for i18n; = JLanguageTool.getMessageBundle(lang)
   * @param config configuration for remote rule server;
   *               options: secure, clientKey, clientCertificate, rootCertificate
                   use RemoteRuleConfig.getRelevantConfig(id, configs)
                   to load this in Language::getRelevantRemoteRules
   * @param id ID of rule
   * @param descriptionKey key in MessageBundle.properties for rule description
   * @param messagesByID mapping match.sub_id -&gt; key in MessageBundle.properties for RuleMatch's message
   * @return instance of RemoteMLRule
   */
  public static GRPCRule create(ResourceBundle messages, RemoteRuleConfig config,
                                String id, String descriptionKey, Map<String, String> messagesByID) {
    return new GRPCRule(messages, config) {


      @Override
      protected String getMessage(MLServerProto.Match match, AnalyzedSentence sentence) {
        return messages.getString(messagesByID.get(match.getSubId()));
      }

      @Override
      public String getDescription() {
        return messages.getString(descriptionKey);
      }
    };
  }

  /**
   * Helper method to create instances of RemoteMLRule
   * @param config configuration for remote rule server;
   *               options: secure, clientKey, clientCertificate, rootCertificate
                   use RemoteRuleConfig.getRelevantConfig(id, configs)
                   to load this in Language::getRelevantRemoteRules
   * @param id ID of rule
   * @param description rule description
   * @param messagesByID mapping match.sub_id to RuleMatch's message
   * @return instance of RemoteMLRule
   */
  public static GRPCRule create(RemoteRuleConfig config,
                                String id, String description, Map<String, String> messagesByID) {
    return new GRPCRule(JLanguageTool.getMessageBundle(), config) {


      @Override
      protected String getMessage(MLServerProto.Match match, AnalyzedSentence sentence) {
        return messagesByID.get(match.getSubId());
      }

      @Override
      public String getDescription() {
        return description;
      }
    };
  }
}
