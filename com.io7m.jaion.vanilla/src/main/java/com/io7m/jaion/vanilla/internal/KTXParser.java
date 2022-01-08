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

import com.io7m.jaion.api.KTX1Header;
import com.io7m.jaion.api.KTXFileReadableType;
import com.io7m.jaion.api.KTXParseRequest;
import com.io7m.jaion.api.KTXParserType;
import com.io7m.jbssio.api.BSSReaderRandomAccessType;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class KTXParser implements KTXParserType
{
  private static final byte[] KTX1_IDENTIFIER = {
    (byte) 0xAB,
    (byte) 0x4B,
    (byte) 0x54,
    (byte) 0x58,
    (byte) 0x20,
    (byte) 0x31,
    (byte) 0x31,
    (byte) 0xBB,
    (byte) 0x0D,
    (byte) 0x0A,
    (byte) 0x1A,
    (byte) 0x0A
  };
  private static final byte[] KTX2_IDENTIFIER = {
    (byte) 0xAB,
    (byte) 0x4B,
    (byte) 0x54,
    (byte) 0x58,
    (byte) 0x20,
    (byte) 0x32,
    (byte) 0x30,
    (byte) 0xBB,
    (byte) 0x0D,
    (byte) 0x0A,
    (byte) 0x1A,
    (byte) 0x0A
  };
  private static final byte[] ENDIAN_BIG = {
    (byte) 0x04,
    (byte) 0x03,
    (byte) 0x02,
    (byte) 0x01
  };
  private static final byte[] ENDIAN_LITTLE = {
    (byte) 0x01,
    (byte) 0x02,
    (byte) 0x03,
    (byte) 0x04
  };

  private final AtomicBoolean closed;
  private final KTXParseRequest request;
  private final BSSReaderRandomAccessType readerInitial;
  private final ArrayDeque<BSSReaderRandomAccessType> readerStack;
  private BSSReaderRandomAccessType reader;
  private boolean isBigEndian;

  public KTXParser(
    final KTXParseRequest inRequest,
    final BSSReaderRandomAccessType inReader)
  {
    this.request =
      Objects.requireNonNull(inRequest, "inRequest");
    this.readerInitial =
      Objects.requireNonNull(inReader, "reader");
    this.readerStack =
      new ArrayDeque<>();
    this.reader =
      this.readerInitial;
    this.closed =
      new AtomicBoolean(false);
  }

  private static String errorUnrecognizedFileIdentifier(
    final byte[] identifier)
  {
    final var lineSeparator = System.lineSeparator();
    final var text = new StringBuilder(128);
    text.append("Unrecognized file identifier.");
    text.append(lineSeparator);
    text.append("  Received:");
    text.append(lineSeparator);
    text.append("    ");
    text.append(formatBytes(identifier));
    text.append(lineSeparator);
    text.append("  Expected one of:");
    text.append(lineSeparator);
    text.append("    ");
    text.append(formatBytes(KTX1_IDENTIFIER));
    text.append(" for KTX 1.0");
    text.append(lineSeparator);
    text.append("    ");
    text.append(formatBytes(KTX2_IDENTIFIER));
    text.append(" for KTX 2.0");
    text.append(lineSeparator);
    return text.toString();
  }

  private static String errorUnrecognizedEndianness(
    final byte[] endianness)
  {
    final var lineSeparator = System.lineSeparator();
    final var text = new StringBuilder(128);
    text.append("Unrecognized endianness value.");
    text.append(lineSeparator);
    text.append("  Received:");
    text.append(lineSeparator);
    text.append("    ");
    text.append(formatBytes(endianness));
    text.append(lineSeparator);
    text.append("  Expected one of:");
    text.append(lineSeparator);
    text.append("    ");
    text.append(formatBytes(ENDIAN_BIG));
    text.append(" for big endian");
    text.append(lineSeparator);
    text.append("    ");
    text.append(formatBytes(ENDIAN_LITTLE));
    text.append(" for little endian");
    text.append(lineSeparator);
    return text.toString();
  }

  private static String formatBytes(
    final byte[] data)
  {
    final var builder = new StringBuilder(24);
    for (final var b : data) {
      builder.append(String.format("%02x", b));
    }
    return builder.toString();
  }

  private String errorLimitExceeded(
    final long length,
    final String limitName)
  {
    final var lineSeparator = System.lineSeparator();
    final var text = new StringBuilder(128);
    text.append("Limit exceeded.");
    text.append(lineSeparator);
    text.append("  At file offset 0x");
    text.append(Long.toUnsignedString(this.reader.offsetCurrentAbsolute(), 16));
    text.append(" we encountered data with a size specified as ");
    text.append(Long.toUnsignedString(length));
    text.append('.');
    text.append(lineSeparator);
    text.append("  The ");
    text.append(limitName);
    text.append(" is configured as ");
    text.append(Long.toUnsignedString(this.request.keyValueRegionLimit()));
    text.append('.');
    text.append(lineSeparator);
    return text.toString();
  }

  @Override
  public KTXFileReadableType execute()
    throws IOException
  {
    if (this.closed.get()) {
      throw new IllegalStateException("Parser is closed.");
    }

    this.readerStack.clear();
    this.reader = this.readerInitial;

    final var identifier = new byte[12];
    this.reader.seekTo(0L);
    this.reader.readBytes(identifier);

    if (Arrays.equals(identifier, KTX2_IDENTIFIER)) {
      return this.executeKTX2();
    }
    if (Arrays.equals(identifier, KTX1_IDENTIFIER)) {
      return this.executeKTX1();
    }

    throw new IOException(errorUnrecognizedFileIdentifier(identifier));
  }

  private KTXFileReadableType executeKTX1()
    throws IOException
  {
    final KTX1Header header;

    try {
      this.startReader("header", 52L);

      final var endianness = new byte[4];
      this.reader.readBytes(endianness);

      if (Arrays.equals(endianness, ENDIAN_BIG)) {
        this.isBigEndian = true;
      } else if (Arrays.equals(endianness, ENDIAN_LITTLE)) {
        this.isBigEndian = false;
      } else {
        throw new IOException(errorUnrecognizedEndianness(endianness));
      }

      final long glType =
        this.readU32("glType");
      final long glTypeSize =
        this.readU32("glTypeSize");
      final long glFormat =
        this.readU32("glFormat");
      final long glInternalFormat =
        this.readU32("glInternalFormat");
      final long glBaseInternalFormat =
        this.readU32("glBaseInternalFormat");
      final long pixelWidth =
        this.readU32("pixelWidth");
      final long pixelHeight =
        this.readU32("pixelHeight");
      final long pixelDepth =
        this.readU32("pixelDepth");
      final long numberOfArrayElements =
        this.readU32("numberOfArrayElements");
      final long numberOfFaces =
        this.readU32("numberOfFaces");
      final long numberOfMipmapLevels =
        this.readU32("numberOfMipmapLevels");
      final long bytesOfKeyValueData =
        this.readU32("bytesOfKeyValueData");

      header = new KTX1Header(
        this.isBigEndian,
        glType,
        glTypeSize,
        glFormat,
        glInternalFormat,
        glBaseInternalFormat,
        pixelWidth,
        pixelHeight,
        pixelDepth,
        numberOfArrayElements,
        minUnsigned(numberOfFaces, 1L),
        numberOfMipmapLevels,
        bytesOfKeyValueData
      );
    } finally {
      this.popReader();
    }

    {
      final var size = header.bytesOfKeyValueData();
      final var limit = this.request.keyValueRegionLimit();
      if (Long.compareUnsigned(size, limit) > 0) {
        throw new IOException(
          this.errorLimitExceeded(size, "key/value region size limit")
        );
      }
    }

    this.reader.seekTo(64L);
    final var keyValues = new HashMap<String, String>();

    final var dataOffset =
      this.readKeyValueDataKTX1(header, keyValues);

    this.reader.seekTo(dataOffset);

    final var images =
      this.readSubImageOffsetsKTX1(header);

    this.closed.set(true);
    return new KTX1FileReadable(this.reader, header, keyValues, dataOffset);
  }

  private List<KTX1SubImage> readSubImageOffsetsKTX1(
    final KTX1Header header)
    throws IOException
  {
    final var subImages = new ArrayList<KTX1SubImage>();

    if (header.numberOfFaces() == 6L && header.numberOfArrayElements() == 0L) {
      return this.readSubImageOffsetsCubeMapKTX1(header);
    }

    final var mipMapCount =
      (int) minUnsigned(header.numberOfMipmapLevels(), 1L);
    final var arrayElementCount =
      (int) minUnsigned(header.numberOfArrayElements(), 1L);
    final var faceCount =
      (int) minUnsigned(header.numberOfFaces(), 1L);
    final var pixelDepth =
      (int) minUnsigned(header.pixelDepth(), 1L);

    for (int mipMapLevel = 0; mipMapLevel < mipMapCount; ++mipMapLevel) {
      final var imageSize = this.readU32("imageSize");

      final var width =
        (int) header.pixelWidth() >> mipMapLevel;
      final var height =
        (int) header.pixelHeight() >> mipMapLevel;

      for (int arrayElement = 0; arrayElement < arrayElementCount; ++arrayElement) {
        for (int faceIndex = 0; faceIndex < faceCount; ++faceIndex) {
          for (int zSlice = 0; zSlice < pixelDepth; ++zSlice) {
            subImages.add(
              new KTX1SubImage(
                this.reader.offsetCurrentAbsolute(),
                imageSize,
                mipMapLevel,
                arrayElement,
                faceIndex,
                zSlice
              )
            );
          }
        }
      }
      this.reader.skip(imageSize);
      this.reader.align(4);
    }

    return List.copyOf(subImages);
  }

  private List<KTX1SubImage> readSubImageOffsetsCubeMapKTX1(
    final KTX1Header header)
  {
    return List.of();
  }

  private static long minUnsigned(
    final long x,
    final long y)
  {
    if (Long.compareUnsigned(x, y) > 0) {
      return x;
    }
    return y;
  }

  private long readKeyValueDataKTX1(
    final KTX1Header header,
    final Map<String, String> keyValues)
    throws IOException
  {
    try {
      this.startReader("keyValueData", header.bytesOfKeyValueData());

      while (true) {
        final var remaining = this.reader.bytesRemaining().orElse(0L);
        if (remaining < 4L) {
          break;
        }

        final var size = this.readU32("keyAndValueByteSize");
        final var limit = this.request.keyValueRegionLimit();
        if (Long.compareUnsigned(size, limit) > 0) {
          throw new IOException(
            this.errorLimitExceeded(size, "key/value datum limit")
          );
        }

        final var data = new byte[(int) size];
        this.reader.readBytes(data);

        var dataStart = 0;
        for (int index = 0; index < data.length; ++index) {
          final var x = data[index];
          if (x == 0) {
            dataStart = index + 1;
            break;
          }
        }

        var dataEnd = data.length - 1;
        for (int index = dataStart; index < data.length; ++index) {
          final var x = data[index];
          if (x == 0) {
            dataEnd = index;
            break;
          }
        }

        final var key =
          new String(data, 0, dataStart - 1, UTF_8);
        final var value =
          new String(data, dataStart, dataEnd - dataStart, UTF_8);

        keyValues.put(key, value);
      }

      this.reader.align(4);
      return this.reader.offsetCurrentAbsolute();
    } finally {
      this.popReader();
    }
  }

  private void popReader()
    throws IOException
  {
    this.reader.close();
    this.reader = this.readerStack.pop();
  }

  private void startReader(
    final String header,
    final long size)
    throws IOException
  {
    this.readerStack.push(this.reader);
    this.reader = this.reader.createSubReaderAtBounded(header, 0L, size);
  }

  private long readU32(
    final String name)
    throws IOException
  {
    if (this.isBigEndian) {
      return this.reader.readU32BE(name);
    }
    return this.reader.readU32LE(name);
  }

  private KTXFileReadableType executeKTX2()
    throws IOException
  {
    throw new IOException();
  }

  @Override
  public void close()
    throws IOException
  {
    if (this.closed.compareAndSet(false, true)) {
      this.reader.close();
    }
  }
}
