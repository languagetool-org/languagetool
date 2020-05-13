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


import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.jetbrains.annotations.Nullable;
import org.languagetool.languagemodel.bert.grpc.BertLmGrpc;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.languagetool.languagemodel.bert.grpc.BertLmProto.*;

public class RemoteLanguageModel {
  private final BertLmGrpc.BertLmBlockingStub model;
  private final ManagedChannel channel;

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
    BatchScoreRequest batch = BatchScoreRequest.newBuilder().addAllRequests(
      requests.stream().map(Request::convert).collect(Collectors.toList())
    ).build();
    // TODO multiple masks
    return model.batchScore(batch).getResponsesList().stream().map(r ->
      r.getScoresList().get(0).getScoreList()).collect(Collectors.toList());
  }

  public List<Double> score(Request req) {
    // TODO deal with max seq length, extract windows
    // TODO mask multiple tokens in a sentence
    // TODO multiple masks
    return model.score(req.convert()).getScoresList().get(0).getScoreList();
  }

}
