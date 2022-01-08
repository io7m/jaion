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

import com.io7m.jaion.api.KTX1FileReadableType;
import com.io7m.jaion.api.KTX1Header;
import com.io7m.jbssio.api.BSSReaderRandomAccessType;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

public final class KTX1FileReadable implements KTX1FileReadableType
{
  private final BSSReaderRandomAccessType reader;
  private final KTX1Header header;
  private final Map<String, String> keyValues;
  private final long dataOffset;

  KTX1FileReadable(
    final BSSReaderRandomAccessType inReader,
    final KTX1Header inHeader,
    final Map<String, String> inKeyValues,
    final long inDataOffset)
  {
    this.reader =
      Objects.requireNonNull(inReader, "reader");
    this.header =
      Objects.requireNonNull(inHeader, "header");
    this.keyValues =
      Map.copyOf(Objects.requireNonNull(inKeyValues, "keyValues"));
    this.dataOffset =
      inDataOffset;
  }

  @Override
  public KTX1Header header()
  {
    return this.header;
  }

  @Override
  public Map<String, String> keyValueData()
  {
    return this.keyValues;
  }

  @Override
  public void close()
    throws IOException
  {
    this.reader.close();
  }
}
