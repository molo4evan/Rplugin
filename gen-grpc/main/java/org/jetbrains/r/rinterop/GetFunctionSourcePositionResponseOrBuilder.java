// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface GetFunctionSourcePositionResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.GetFunctionSourcePositionResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.rplugininterop.SourcePosition position = 1;</code>
   */
  boolean hasPosition();
  /**
   * <code>.rplugininterop.SourcePosition position = 1;</code>
   */
  org.jetbrains.r.rinterop.SourcePosition getPosition();
  /**
   * <code>.rplugininterop.SourcePosition position = 1;</code>
   */
  org.jetbrains.r.rinterop.SourcePositionOrBuilder getPositionOrBuilder();

  /**
   * <code>string sourcePositionText = 2;</code>
   */
  java.lang.String getSourcePositionText();
  /**
   * <code>string sourcePositionText = 2;</code>
   */
  com.google.protobuf.ByteString
      getSourcePositionTextBytes();
}