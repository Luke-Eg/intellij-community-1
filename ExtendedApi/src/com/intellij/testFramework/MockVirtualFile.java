package com.intellij.testFramework;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.util.LocalTimeCounter;
import junit.framework.Assert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class MockVirtualFile extends VirtualFile {
  private FileType myFileType;
  protected CharSequence myContent = "";
  protected String myName = "";
  public long myModStamp = LocalTimeCounter.currentTime();
  protected long myTimeStamp = System.currentTimeMillis();
  protected long myActualTimeStamp = myTimeStamp;
  private boolean myIsWritable = true;
  private VirtualFileListener myListener = null;
  @NonNls private static final Charset CHARSET = Charset.forName("UTF-8");

  public MockVirtualFile() {
  }

  public MockVirtualFile(String name) {
    myName = name;
  }

  public MockVirtualFile(String name, CharSequence content) {
    myName = name;
    myContent = content;
  }

  public MockVirtualFile(final String name, final FileType fileType, final CharSequence text) {
    this(name, fileType, text, LocalTimeCounter.currentTime());
  }

  public MockVirtualFile(final String name, final FileType fileType, final CharSequence text, final long modificationStamp) {
    myName = name;
    myFileType = fileType;
    myContent = text;
    myModStamp = modificationStamp;
  }

  public void setListener(VirtualFileListener listener) {
    myListener = listener;
  }

  private static class MyVirtualFileSystem extends VirtualFileSystem {
    @NonNls private final static String PROTOCOL = "mock";

    public String getProtocol() {
      return PROTOCOL;
    }

    @Nullable
    public VirtualFile findFileByPath(String path) {
      return null;
    }

    public void refresh(boolean asynchronous) {}

    @Nullable
    public VirtualFile refreshAndFindFileByPath(String path) {
      return null;
    }

    public void forceRefreshFiles(final boolean asynchronous, @NotNull VirtualFile... files) {}

    protected void deleteFile(Object requestor, VirtualFile vFile) throws IOException {}

    protected void moveFile(Object requestor, VirtualFile vFile, VirtualFile newParent) throws IOException {}

    protected void renameFile(Object requestor, VirtualFile vFile, String newName) throws IOException {}

    protected VirtualFile createChildFile(Object requestor, VirtualFile vDir, String fileName) throws IOException {
      throw new IOException("Cannot create files");
    }

    protected VirtualFile createChildDirectory(Object requestor, VirtualFile vDir, String dirName) throws IOException {
      throw new IOException("Cannot create directories");
    }
  }

  private static MyVirtualFileSystem ourFileSystem = new MyVirtualFileSystem();

  @NotNull
  public VirtualFileSystem getFileSystem() {
    return ourFileSystem;
  }

  public FileType getFileType() {
    return myFileType != null ? myFileType : super.getFileType();
  }

  public String getPath() {
    return "/" + getName();
  }

  @NotNull
  public String getName() {
    return myName;
  }

  public boolean isWritable() {
    return myIsWritable;
  }

  public boolean isDirectory() {
    return false;
  }

  public boolean isValid() {
    return true;
  }

  public VirtualFile getParent() {
    return null;
  }

  public VirtualFile[] getChildren() {
    return VirtualFile.EMPTY_ARRAY;
  }

  public InputStream getInputStream() throws IOException {
    throw new IOException("Cannot get input stream");
  }

  public OutputStream getOutputStream(Object requestor, final long newModificationStamp, long newTimeStamp) throws IOException {
    return new ByteArrayOutputStream() {
      public void close() throws IOException {
        myModStamp = newModificationStamp;
        myContent = toString();
      }
    };
  }

  public byte[] contentsToByteArray() throws IOException {
    return getContent().toString().getBytes(getCharset().name());
  }

  public long getModificationStamp() {
    return myModStamp;
  }

  public long getTimeStamp() {
    return myTimeStamp;
  }

  public void setActualTimeStamp(long actualTimeStamp) {
    myActualTimeStamp = actualTimeStamp;
  }

  public long getActualTimeStamp() {
    return myActualTimeStamp;
  }

  public long getLength() {
    try {
      return contentsToByteArray().length;
    }
    catch (IOException e) {
      e.printStackTrace();
      Assert.assertTrue(false);
      return 0;
    }
  }

  public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
  }

  public void setContent(Object requestor, String content, boolean fireEvent) {
    long oldStamp = myModStamp;
    myContent = content;
    if (fireEvent) {
      myModStamp = LocalTimeCounter.currentTime();
      myListener.contentsChanged(new VirtualFileEvent(requestor, this, null, oldStamp, myModStamp));
    }
  }

  public VirtualFile self() {
    return this;
  }

  public void setWritable(boolean b) {
    myIsWritable = b;
  }

  public void rename(Object requestor, String newName) throws IOException {
    myName = newName;
  }

  protected CharSequence getContent() {
    return myContent;
  }

  public Charset getCharset() {
    return CHARSET;
  }
}
