// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

/**
 * Protobuf type {@code rplugininterop.SourcePosition}
 */
public final class SourcePosition extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:rplugininterop.SourcePosition)
    SourcePositionOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SourcePosition.newBuilder() to construct.
  private SourcePosition(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SourcePosition() {
    fileId_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new SourcePosition();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private SourcePosition(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            java.lang.String s = input.readStringRequireUtf8();

            fileId_ = s;
            break;
          }
          case 16: {

            line_ = input.readInt32();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_SourcePosition_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_SourcePosition_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.jetbrains.r.rinterop.SourcePosition.class, org.jetbrains.r.rinterop.SourcePosition.Builder.class);
  }

  public static final int FILEID_FIELD_NUMBER = 1;
  private volatile java.lang.Object fileId_;
  /**
   * <code>string fileId = 1;</code>
   * @return The fileId.
   */
  @java.lang.Override
  public java.lang.String getFileId() {
    java.lang.Object ref = fileId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      fileId_ = s;
      return s;
    }
  }
  /**
   * <code>string fileId = 1;</code>
   * @return The bytes for fileId.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString
      getFileIdBytes() {
    java.lang.Object ref = fileId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      fileId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int LINE_FIELD_NUMBER = 2;
  private int line_;
  /**
   * <code>int32 line = 2;</code>
   * @return The line.
   */
  @java.lang.Override
  public int getLine() {
    return line_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!getFileIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, fileId_);
    }
    if (line_ != 0) {
      output.writeInt32(2, line_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getFileIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, fileId_);
    }
    if (line_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, line_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.jetbrains.r.rinterop.SourcePosition)) {
      return super.equals(obj);
    }
    org.jetbrains.r.rinterop.SourcePosition other = (org.jetbrains.r.rinterop.SourcePosition) obj;

    if (!getFileId()
        .equals(other.getFileId())) return false;
    if (getLine()
        != other.getLine()) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + FILEID_FIELD_NUMBER;
    hash = (53 * hash) + getFileId().hashCode();
    hash = (37 * hash) + LINE_FIELD_NUMBER;
    hash = (53 * hash) + getLine();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.jetbrains.r.rinterop.SourcePosition parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.jetbrains.r.rinterop.SourcePosition prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code rplugininterop.SourcePosition}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:rplugininterop.SourcePosition)
      org.jetbrains.r.rinterop.SourcePositionOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_SourcePosition_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_SourcePosition_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.jetbrains.r.rinterop.SourcePosition.class, org.jetbrains.r.rinterop.SourcePosition.Builder.class);
    }

    // Construct using org.jetbrains.r.rinterop.SourcePosition.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      fileId_ = "";

      line_ = 0;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.jetbrains.r.rinterop.Service.internal_static_rplugininterop_SourcePosition_descriptor;
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.SourcePosition getDefaultInstanceForType() {
      return org.jetbrains.r.rinterop.SourcePosition.getDefaultInstance();
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.SourcePosition build() {
      org.jetbrains.r.rinterop.SourcePosition result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.jetbrains.r.rinterop.SourcePosition buildPartial() {
      org.jetbrains.r.rinterop.SourcePosition result = new org.jetbrains.r.rinterop.SourcePosition(this);
      result.fileId_ = fileId_;
      result.line_ = line_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.jetbrains.r.rinterop.SourcePosition) {
        return mergeFrom((org.jetbrains.r.rinterop.SourcePosition)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.jetbrains.r.rinterop.SourcePosition other) {
      if (other == org.jetbrains.r.rinterop.SourcePosition.getDefaultInstance()) return this;
      if (!other.getFileId().isEmpty()) {
        fileId_ = other.fileId_;
        onChanged();
      }
      if (other.getLine() != 0) {
        setLine(other.getLine());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.jetbrains.r.rinterop.SourcePosition parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.r.rinterop.SourcePosition) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object fileId_ = "";
    /**
     * <code>string fileId = 1;</code>
     * @return The fileId.
     */
    public java.lang.String getFileId() {
      java.lang.Object ref = fileId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        fileId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string fileId = 1;</code>
     * @return The bytes for fileId.
     */
    public com.google.protobuf.ByteString
        getFileIdBytes() {
      java.lang.Object ref = fileId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        fileId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string fileId = 1;</code>
     * @param value The fileId to set.
     * @return This builder for chaining.
     */
    public Builder setFileId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      fileId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string fileId = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearFileId() {
      
      fileId_ = getDefaultInstance().getFileId();
      onChanged();
      return this;
    }
    /**
     * <code>string fileId = 1;</code>
     * @param value The bytes for fileId to set.
     * @return This builder for chaining.
     */
    public Builder setFileIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      fileId_ = value;
      onChanged();
      return this;
    }

    private int line_ ;
    /**
     * <code>int32 line = 2;</code>
     * @return The line.
     */
    @java.lang.Override
    public int getLine() {
      return line_;
    }
    /**
     * <code>int32 line = 2;</code>
     * @param value The line to set.
     * @return This builder for chaining.
     */
    public Builder setLine(int value) {
      
      line_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 line = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearLine() {
      
      line_ = 0;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:rplugininterop.SourcePosition)
  }

  // @@protoc_insertion_point(class_scope:rplugininterop.SourcePosition)
  private static final org.jetbrains.r.rinterop.SourcePosition DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.jetbrains.r.rinterop.SourcePosition();
  }

  public static org.jetbrains.r.rinterop.SourcePosition getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SourcePosition>
      PARSER = new com.google.protobuf.AbstractParser<SourcePosition>() {
    @java.lang.Override
    public SourcePosition parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new SourcePosition(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<SourcePosition> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SourcePosition> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.jetbrains.r.rinterop.SourcePosition getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

