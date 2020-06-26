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
package org.languagetool.languagemodel.bert;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.jetbrains.annotations.Nullable;
import org.languagetool.languagemodel.bert.grpc.BertLmGrpc;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.languagetool.languagemodel.bert.grpc.BertLmProto.*;

public class RemoteLanguageModel {

  private final BertLmGrpc.BertLmBlockingStub model;
  private final ManagedChannel channel;
  private final Cache<Request, List<Double>> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .build();

  public static class Request {
    public String text;
    public int start;
    public int end;
    public List<String> candidates;

    public Request(String text, int start, int end, List<String> candidates) {
      this.text = text;
      this.start = start;
      this.end = end;
      this.candidates = candidates;
    }

    public ScoreRequest convert() {
      List<Mask> masks = Arrays.asList(Mask.newBuilder()
        .setStart(start)
        .setEnd(end)
        .addAllCandidates(candidates)
        .build());
      return ScoreRequest.newBuilder().setText(text).addAllMask(masks).build();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Request request = (Request) o;
      return start == request.start &&
        end == request.end &&
        text.equals(request.text) &&
        candidates.equals(request.candidates);
    }

    @Override
    public int hashCode() {
      return Objects.hash(text, start, end, candidates);
    }

  }

  public RemoteLanguageModel(String host, int port, boolean useSSL,
                             @Nullable String clientPrivateKey, @Nullable  String clientCertificate,
                             @Nullable String rootCertificate) throws SSLException {
    // TODO configure deadline/retries/... here?
    channel = getChannel(host, port, useSSL, clientPrivateKey, clientCertificate, rootCertificate);
    model = BertLmGrpc.newBlockingStub(channel);
  }

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

  public void shutdown() {
    if (channel != null) {
      channel.shutdownNow();
    }
  }

  public List<List<Double>> batchScore(List<Request> requests) {
    Map<Request, List<Double>> cachedRequests = new HashMap<>();
    List<Request> uncachedRequests = new ArrayList<>();
    for (Request request : requests) {
      List<Double> result = cache.getIfPresent(request);
      if (result == null) {
        uncachedRequests.add(request);
      } else {
        cachedRequests.put(request, result);
      }
    }
    BatchScoreRequest batch = BatchScoreRequest.newBuilder().addAllRequests(
      uncachedRequests.stream().map(Request::convert).collect(Collectors.toList())
    ).build();
    // TODO multiple masks
    List<List<Double>> nonCacheResult = model.batchScore(batch).getResponsesList().stream().map(r ->
      r.getScoresList().get(0).getScoreList()).collect(Collectors.toList());
    //
    List<List<Double>> allResults = new ArrayList<>();
    int i = 0;
    for (Request request : requests) {
      List<Double> result = cachedRequests.get(request);
      if (result != null) {
        //System.out.println("Adding result from cache");
        allResults.add(result);
      } else {
        //System.out.println("Adding result from remote");
        allResults.add(nonCacheResult.get(i++));
      }
    }

    int j = 0;
    for (List<Double> re : nonCacheResult) {
      // a CacheLoader doesn't work with batching, so add manually:
      //System.out.println("Adding request to cache");
      cache.put(uncachedRequests.get(j), re);
      j++;
    }
    return allResults;
  }

  public List<Double> score(Request req) {
    // TODO deal with max seq length, extract windows
    // TODO mask multiple tokens in a sentence
    // TODO multiple masks
    return model.score(req.convert()).getScoresList().get(0).getScoreList();
  }

}
