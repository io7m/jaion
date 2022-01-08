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

package com.io7m.jaion.tests;

import com.io7m.jaion.vanilla.internal.KTXSubrangeByteChannel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.file.StandardOpenOption.READ;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class KTXSubrangeByteChannelTest
{
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.directory = KTXTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    KTXTestDirectories.deleteDirectory(this.directory);
  }

  @Test
  public void testSubrange0()
    throws IOException
  {
    final var file0 =
      this.directory.resolve("hello0.txt");

    Files.writeString(file0, "AABBCCDD");

    final var channel0 =
      FileChannel.open(file0, READ);

    try (var subChannel =
           new KTXSubrangeByteChannel(channel0, 1L, 6L)) {
      assertTrue(subChannel.isOpen());
      assertEquals(6L, subChannel.size());

      int read;
      final var byteBuffer = ByteBuffer.allocate(8);
      final var array = byteBuffer.array();
      Arrays.fill(array, (byte) 0xff);

      subChannel.position(0L);
      read = subChannel.read(byteBuffer);
      assertEquals(6, read);
      assertEquals(6L, subChannel.position());

      assertEquals('A', array[0] & 0xff);
      assertEquals('B', array[1] & 0xff);
      assertEquals('B', array[2] & 0xff);
      assertEquals('C', array[3] & 0xff);
      assertEquals('C', array[4] & 0xff);
      assertEquals('D', array[5] & 0xff);
      assertEquals(0xff, array[6] & 0xff);
      assertEquals(0xff, array[7] & 0xff);
      Arrays.fill(array, (byte) 0xff);

      byteBuffer.rewind();
      subChannel.position(1L);
      read = subChannel.read(byteBuffer);
      assertEquals(5, read);
      assertEquals(6L, subChannel.position());

      assertEquals('B', array[0] & 0xff);
      assertEquals('B', array[1] & 0xff);
      assertEquals('C', array[2] & 0xff);
      assertEquals('C', array[3] & 0xff);
      assertEquals('D', array[4] & 0xff);
      assertEquals(0xff, array[5] & 0xff);
      assertEquals(0xff, array[6] & 0xff);
      assertEquals(0xff, array[7] & 0xff);
      Arrays.fill(array, (byte) 0xff);

      byteBuffer.rewind();
      subChannel.position(6L);
      read = subChannel.read(byteBuffer);
      assertEquals(0, read);
      assertEquals(6L, subChannel.position());

      assertEquals(0xff, array[0] & 0xff);
      assertEquals(0xff, array[1] & 0xff);
      assertEquals(0xff, array[2] & 0xff);
      assertEquals(0xff, array[3] & 0xff);
      assertEquals(0xff, array[4] & 0xff);
      assertEquals(0xff, array[5] & 0xff);
      assertEquals(0xff, array[6] & 0xff);
      assertEquals(0xff, array[7] & 0xff);
    }

    assertTrue(channel0.isOpen());
  }

  @Test
  public void testReadOnlyWrite()
    throws IOException
  {
    final var file0 =
      this.directory.resolve("hello0.txt");

    Files.writeString(file0, "AABBCCDD");

    final var channel0 =
      FileChannel.open(file0, READ);

    try (var subChannel =
           new KTXSubrangeByteChannel(channel0, 1L, 6L)) {
      assertTrue(subChannel.isOpen());

      final var byteBuffer = ByteBuffer.allocate(8);
      final var array = byteBuffer.array();
      Arrays.fill(array, (byte) 0xff);

      assertThrows(NonWritableChannelException.class, () -> {
        subChannel.write(byteBuffer);
      });
      assertThrows(NonWritableChannelException.class, () -> {
        subChannel.truncate(0L);
      });
    }

    assertTrue(channel0.isOpen());
  }
}
