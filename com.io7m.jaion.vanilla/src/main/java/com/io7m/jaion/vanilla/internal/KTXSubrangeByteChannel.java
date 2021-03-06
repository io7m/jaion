/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jaion.vanilla.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public final class KTXSubrangeByteChannel implements SeekableByteChannel
{
  private final SeekableByteChannel delegate;
  private final long baseStart;
  private final long limit;
  private final long baseEnd;

  public KTXSubrangeByteChannel(
    final SeekableByteChannel inDelegate,
    final long inBase,
    final long inLimit)
  {
    this.delegate =
      Objects.requireNonNull(inDelegate, "delegate");

    this.baseStart = inBase;
    this.baseEnd = this.baseStart + inLimit;
    this.limit = inLimit;
  }

  private long channelRemaining()
    throws IOException
  {
    return this.baseEnd - this.delegate.position();
  }

  @Override
  public int read(
    final ByteBuffer dst)
    throws IOException
  {
    final var bufferRemaining =
      dst.remaining();
    final var channelRemaining =
      this.channelRemaining();
    final var remaining =
      Math.min(bufferRemaining, channelRemaining);
    final var bufferDifference =
      bufferRemaining - remaining;
    final var newLimit =
      (int) (dst.limit() - bufferDifference);

    dst.limit(newLimit);
    return this.delegate.read(dst);
  }

  @Override
  public int write(
    final ByteBuffer src)
  {
    throw new NonWritableChannelException();
  }

  @Override
  public long position()
    throws IOException
  {
    return this.delegate.position() - this.baseStart;
  }

  @Override
  public SeekableByteChannel position(
    final long newPosition)
    throws IOException
  {
    return this.delegate.position(this.baseStart + newPosition);
  }

  @Override
  public long size()
  {
    return this.limit;
  }

  @Override
  public SeekableByteChannel truncate(
    final long size)
  {
    throw new NonWritableChannelException();
  }

  @Override
  public boolean isOpen()
  {
    return this.delegate.isOpen();
  }

  @Override
  public void close()
  {

  }
}
