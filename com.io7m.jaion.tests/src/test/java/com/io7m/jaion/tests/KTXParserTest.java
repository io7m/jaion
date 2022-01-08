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

import com.io7m.jaion.api.KTX1Header;
import com.io7m.jaion.api.KTXParseRequest;
import com.io7m.jaion.api.KTXParserType;
import com.io7m.jaion.vanilla.KTXParserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class KTXParserTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(KTXParserTest.class);

  private KTXParserFactory readers;
  private Path directory;

  @BeforeEach
  public void setup()
    throws IOException
  {
    this.readers =
      new KTXParserFactory();
    this.directory =
      KTXTestDirectories.createTempDirectory();
  }

  @AfterEach
  public void tearDown()
    throws IOException
  {
    KTXTestDirectories.deleteDirectory(this.directory);
  }

  @Test
  public void testRGBAReferenceLittleKTX1()
    throws Exception
  {
    try (var parser = this.parserFor("rgba-reference-little.ktx")) {
      try (var file = parser.execute()) {
        assertEquals(0, file.keyValueData().size());
        final KTX1Header header = (KTX1Header) file.header();
        assertFalse(header.isBigEndian());
        assertEquals(0x1401, header.glType());
        assertEquals(1, header.glTypeSize());
        assertEquals(0x1908, header.glFormat());
        assertEquals(0x8c43, header.glInternalFormat());
        assertEquals(0x1908, header.glBaseInternalFormat());
        assertEquals(128, header.pixelWidth());
        assertEquals(128, header.pixelHeight());
        assertEquals(0, header.pixelDepth());
        assertEquals(0, header.numberOfArrayElements());
        assertEquals(1, header.numberOfFaces());
        assertEquals(1, header.numberOfMipmapLevels());
        assertEquals(0, header.bytesOfKeyValueData());
      }
    }
  }

  @Test
  public void testRGBAReferenceBigKTX1()
    throws Exception
  {
    try (var parser = this.parserFor("rgba-reference-big.ktx")) {
      try (var file = parser.execute()) {
        assertEquals(0, file.keyValueData().size());
        final KTX1Header header = (KTX1Header) file.header();
        assertTrue(header.isBigEndian());
        assertEquals(0x1401, header.glType());
        assertEquals(1, header.glTypeSize());
        assertEquals(0x1908, header.glFormat());
        assertEquals(0x8c43, header.glInternalFormat());
        assertEquals(0x1908, header.glBaseInternalFormat());
        assertEquals(128, header.pixelWidth());
        assertEquals(128, header.pixelHeight());
        assertEquals(0, header.pixelDepth());
        assertEquals(0, header.numberOfArrayElements());
        assertEquals(1, header.numberOfFaces());
        assertEquals(1, header.numberOfMipmapLevels());
        assertEquals(0, header.bytesOfKeyValueData());
      }
    }
  }

  @Test
  public void testOrangeKTX1()
    throws Exception
  {
    try (var parser = this.parserFor("orange.ktx")) {
      try (var file = parser.execute()) {
        assertEquals(1, file.keyValueData().size());
        assertEquals("S=r,T=d", file.keyValueData().get("KTXorientation"));
        final KTX1Header header = (KTX1Header) file.header();
        assertFalse(header.isBigEndian());
        assertEquals(0x1401, header.glType());
        assertEquals(1, header.glTypeSize());
        assertEquals(0x1908, header.glFormat());
        assertEquals(0x8c43, header.glInternalFormat());
        assertEquals(0x1908, header.glBaseInternalFormat());
        assertEquals(18, header.pixelWidth());
        assertEquals(18, header.pixelHeight());
        assertEquals(0, header.pixelDepth());
        assertEquals(0, header.numberOfArrayElements());
        assertEquals(1, header.numberOfFaces());
        assertEquals(1, header.numberOfMipmapLevels());
        assertEquals(28, header.bytesOfKeyValueData());
      }
    }
  }

  @Test
  public void testRGBMipmapsKTX1()
    throws Exception
  {
    try (var parser = this.parserFor("rgb-mipmap-reference.ktx")) {
      try (var file = parser.execute()) {
        assertEquals(0, file.keyValueData().size());
        final KTX1Header header = (KTX1Header) file.header();
        assertFalse(header.isBigEndian());
        assertEquals(0x1401, header.glType());
        assertEquals(1, header.glTypeSize());
        assertEquals(0x1907, header.glFormat());
        assertEquals(0x8c41, header.glInternalFormat());
        assertEquals(0x1907, header.glBaseInternalFormat());
        assertEquals(64, header.pixelWidth());
        assertEquals(64, header.pixelHeight());
        assertEquals(0, header.pixelDepth());
        assertEquals(0, header.numberOfArrayElements());
        assertEquals(1, header.numberOfFaces());
        assertEquals(7, header.numberOfMipmapLevels());
        assertEquals(0, header.bytesOfKeyValueData());
      }
    }
  }

  @Test
  public void testTextureArrayASTC8x8UnormKTX1()
    throws Exception
  {
    try (var parser = this.parserFor("texturearray_astc_8x8_unorm.ktx")) {
      try (var file = parser.execute()) {
        assertEquals(1, file.keyValueData().size());
        assertEquals("S=r,T=d,R=i", file.keyValueData().get("KTXorientation"));
        final KTX1Header header = (KTX1Header) file.header();
        assertFalse(header.isBigEndian());
        assertEquals(0, header.glType());
        assertEquals(1, header.glTypeSize());
        assertEquals(0, header.glFormat());
        assertEquals(0x93B7, header.glInternalFormat());
        assertEquals(0x1908, header.glBaseInternalFormat());
        assertEquals(256, header.pixelWidth());
        assertEquals(256, header.pixelHeight());
        assertEquals(0, header.pixelDepth());
        assertEquals(7, header.numberOfArrayElements());
        assertEquals(1, header.numberOfFaces());
        assertEquals(1, header.numberOfMipmapLevels());
        assertEquals(32, header.bytesOfKeyValueData());
      }
    }
  }

  @Test
  public void testOrangeClosed0()
    throws Exception
  {
    try (var parser = this.parserFor("orange.ktx")) {
      final var file = parser.execute();
      file.close();
      assertThrows(IllegalStateException.class, parser::execute);
    }
  }

  @Test
  public void testOrangeClosed1()
    throws Exception
  {
    final var parser = this.parserFor("orange.ktx");
    parser.execute();
    assertThrows(IllegalStateException.class, parser::execute);
  }

  @Test
  public void testBroken1()
    throws Exception
  {
    try (var parser = this.parserFor("broken1.ktx")) {
      final var ex = assertThrows(IOException.class, parser::execute);
      LOG.debug("exception: ", ex);
      assertTrue(ex.getMessage().contains("Unrecognized file identifier."));
    }
  }

  @Test
  public void testBrokenOrangeTruncated0()
    throws Exception
  {
    try (var parser = this.parserFor("broken-orange-truncated-0.ktx")) {
      final var ex = assertThrows(IOException.class, parser::execute);
      LOG.debug("exception: ", ex);
      assertTrue(ex.getMessage().contains("Out of bounds."));
    }
  }

  @Test
  public void testBrokenOrangeTruncated1()
    throws Exception
  {
    try (var parser = this.parserFor("broken-orange-truncated-1.ktx")) {
      final var ex = assertThrows(IOException.class, parser::execute);
      LOG.debug("exception: ", ex);
      assertTrue(ex.getMessage().contains("Out of bounds."));
    }
  }

  @Test
  public void testBrokenKeyDataTooLarge()
    throws Exception
  {
    try (var parser = this.parserFor("broken-keydata-too-large.ktx")) {
      final var ex = assertThrows(IOException.class, parser::execute);
      LOG.debug("exception: ", ex);
      assertTrue(ex.getMessage().contains("Limit exceeded."));
      assertTrue(ex.getMessage().contains("key/value region size limit"));
    }
  }

  @Test
  public void testBrokenKeyDataValueTooLarge()
    throws Exception
  {
    try (var parser = this.parserFor("broken-keydata-value-too-large.ktx")) {
      final var ex = assertThrows(IOException.class, parser::execute);
      LOG.debug("exception: ", ex);
      assertTrue(ex.getMessage().contains("Limit exceeded."));
      assertTrue(ex.getMessage().contains("key/value datum limit"));
    }
  }

  @Test
  public void testBrokenUnrecognizedEndianness()
    throws Exception
  {
    try (var parser = this.parserFor("broken-rgba-reference-endianness.ktx")) {
      final var ex = assertThrows(IOException.class, parser::execute);
      LOG.debug("exception: ", ex);
      assertTrue(ex.getMessage().contains("Unrecognized endianness value."));
    }
  }

  private KTXParserType parserFor(
    final String name)
    throws IOException
  {
    final var file =
      KTXTestDirectories.resourceOf(
        KTXParserTest.class,
        this.directory,
        name
      );

    final var channel =
      FileChannel.open(file, StandardOpenOption.READ);

    return this.readers.create(
      KTXParseRequest.builder(channel, file.toUri())
        .build()
    );
  }
}
