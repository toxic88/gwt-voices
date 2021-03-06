/*
 * Copyright 2010 Fred Sauer
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.allen_sauer.gwt.voices.demo.client;

// CHECKSTYLE_JAVADOC_OFF
public class WikipediaSound extends ThirdPartySound {
  private final String actualURL;
  private final String audioFormatDescription;
  private final String originalWikipediaFilename;
  private final String wikipediaFileURL;
  private final String wikipediaPageURL;

  public WikipediaSound(String mimeType, String actualURL, String originalWikipediaFilename,
      String wikipediaFileURL, String wikipediaPageURL, String audioFormatDescription) {
    super(mimeType);
    this.originalWikipediaFilename = originalWikipediaFilename;
    this.actualURL = actualURL;
    this.wikipediaFileURL = wikipediaFileURL;
    this.wikipediaPageURL = wikipediaPageURL;
    this.audioFormatDescription = audioFormatDescription;
  }

  @Override
  public String getActualURL() {
    return actualURL;
  }

  public String getFreesoundFileURL() {
    return wikipediaFileURL;
  }

  public String getWikipediaPageURL() {
    return wikipediaPageURL;
  }

  @Override
  public String toHTMLString() {
    return "<a href='" + getFreesoundFileURL() + "'>" + originalWikipediaFilename
        + "</a> from <a href='" + getWikipediaPageURL() + "'>" + getWikipediaPageURL() + "</a>; "
        + audioFormatDescription;
  }

  @Override
  public String toString() {
    return originalWikipediaFilename;
  }

  @Override
  public ThirdPartySound copyOf() {
    return new WikipediaSound(getMimeType(), actualURL, originalWikipediaFilename,
        wikipediaFileURL, wikipediaPageURL, audioFormatDescription);
  }
}
