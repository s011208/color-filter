/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/yenhsunhuang/Documents/workspace/color-filter/com.yenhsun.colorfilter/src/com/yenhsun/colorfilter/IColorChanged.aidl
 */
package com.yenhsun.colorfilter;
public interface IColorChanged extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.yenhsun.colorfilter.IColorChanged
{
private static final java.lang.String DESCRIPTOR = "com.yenhsun.colorfilter.IColorChanged";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.yenhsun.colorfilter.IColorChanged interface,
 * generating a proxy if needed.
 */
public static com.yenhsun.colorfilter.IColorChanged asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.yenhsun.colorfilter.IColorChanged))) {
return ((com.yenhsun.colorfilter.IColorChanged)iin);
}
return new com.yenhsun.colorfilter.IColorChanged.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_changeBackground:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.changeBackground(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.yenhsun.colorfilter.IColorChanged
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void changeBackground(int color) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(color);
mRemote.transact(Stub.TRANSACTION_changeBackground, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_changeBackground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void changeBackground(int color) throws android.os.RemoteException;
}
