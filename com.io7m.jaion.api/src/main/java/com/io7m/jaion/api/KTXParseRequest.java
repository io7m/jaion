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

package com.io7m.jaion.api;

import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

public final record KTXParseRequest(
  SeekableByteChannel channel,
  URI source,
  long keyValueRegionLimit,
  long keyValueDatumLimit)
{
  public KTXParseRequest
  {
    Objects.requireNonNull(channel, "channel");
    Objects.requireNonNull(source, "source");
  }

  public static KTXParseRequestBuilderType builder(
    final SeekableByteChannel inChannel,
    final URI inSource)
  {
    return new Builder(inChannel, inSource);
  }

  private static final class Builder
    implements KTXParseRequestBuilderType
  {
    private SeekableByteChannel channel;
    private URI source;
    private long keyValueRegionLimit = 10_000_000L;
    private long keyValueDatumLimit = 1_000_000L;

    private Builder(
      final SeekableByteChannel inChannel,
      final URI inSource)
    {
      this.channel =
        Objects.requireNonNull(inChannel, "channel");
      this.source =
        Objects.requireNonNull(inSource, "source");
    }

    @Override
    public SeekableByteChannel channel()
    {
      return this.channel;
    }

    @Override
    public void setChannel(
      final SeekableByteChannel inChannel)
    {
      this.channel =
        Objects.requireNonNull(inChannel, "channel");
    }

    @Override
    public URI source()
    {
      return this.source;
    }

    @Override
    public void setSource(
      final URI inSource)
    {
      this.source =
        Objects.requireNonNull(inSource, "source");
    }

    @Override
    public long keyValueRegionLimit()
    {
      return this.keyValueRegionLimit;
    }

    @Override
    public void setKeyValueRegionLimit(
      final long limit)
    {
      this.keyValueRegionLimit = limit;
    }

    @Override
    public long keyValueDatumLimit()
    {
      return this.keyValueDatumLimit;
    }

    @Override
    public void setKeyValueDatumLimit(
      final long limit)
    {
      this.keyValueDatumLimit = limit;
    }

    @Override
    public KTXParseRequest build()
    {
      return new KTXParseRequest(
        this.channel,
        this.source,
        this.keyValueRegionLimit,
        this.keyValueDatumLimit
      );
    }
  }
}
