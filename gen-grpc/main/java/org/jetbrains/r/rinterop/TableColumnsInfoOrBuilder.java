// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: service.proto

package org.jetbrains.r.rinterop;

public interface TableColumnsInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rplugininterop.TableColumnsInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .rplugininterop.TableColumnsInfo.Column columns = 1;</code>
   */
  java.util.List<org.jetbrains.r.rinterop.TableColumnsInfo.Column> 
      getColumnsList();
  /**
   * <code>repeated .rplugininterop.TableColumnsInfo.Column columns = 1;</code>
   */
  org.jetbrains.r.rinterop.TableColumnsInfo.Column getColumns(int index);
  /**
   * <code>repeated .rplugininterop.TableColumnsInfo.Column columns = 1;</code>
   */
  int getColumnsCount();
  /**
   * <code>repeated .rplugininterop.TableColumnsInfo.Column columns = 1;</code>
   */
  java.util.List<? extends org.jetbrains.r.rinterop.TableColumnsInfo.ColumnOrBuilder> 
      getColumnsOrBuilderList();
  /**
   * <code>repeated .rplugininterop.TableColumnsInfo.Column columns = 1;</code>
   */
  org.jetbrains.r.rinterop.TableColumnsInfo.ColumnOrBuilder getColumnsOrBuilder(
      int index);

  /**
   * <code>.rplugininterop.TableColumnsInfo.TableType tableType = 2;</code>
   * @return The enum numeric value on the wire for tableType.
   */
  int getTableTypeValue();
  /**
   * <code>.rplugininterop.TableColumnsInfo.TableType tableType = 2;</code>
   * @return The tableType.
   */
  org.jetbrains.r.rinterop.TableColumnsInfo.TableType getTableType();
}
