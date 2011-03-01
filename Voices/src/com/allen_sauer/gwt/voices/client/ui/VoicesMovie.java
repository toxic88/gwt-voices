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
package com.allen_sauer.gwt.voices.client.ui;

import static com.allen_sauer.gwt.voices.client.Sound.LoadState.LOAD_STATE_NOT_SUPPORTED;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_NOT_SUPPORTED;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_NOT_READY;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_READY;
import static com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport.MIME_TYPE_SUPPORT_UNKNOWN;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import com.allen_sauer.gwt.voices.client.FlashSound;
import com.allen_sauer.gwt.voices.client.Sound;
import com.allen_sauer.gwt.voices.client.SoundController.MimeTypeSupport;
import com.allen_sauer.gwt.voices.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;

// CHECKSTYLE_JAVADOC_OFF
public class VoicesMovie extends FlashMovie {

  @SuppressWarnings("deprecation")
  private static final String[] FLASH_SUPPORTED_MIME_TYPES = {
      Sound.MIME_TYPE_AUDIO_MPEG, Sound.MIME_TYPE_AUDIO_MPEG_MP3};
  private static final String GWT_VOICES_SWF = GWT.getModuleBaseURL() + "gwt-voices.swf";

  private MimeTypeSupport flashSupport = MIME_TYPE_SUPPORT_UNKNOWN;
  private final ArrayList<FlashSound> unitializedSoundList = new ArrayList<FlashSound>();

  public VoicesMovie(String id) {
    super(id, GWT_VOICES_SWF);
    installFlashCallbackHooks(id);

    // Flash Player version check for ExternalInterface support
    if (isExternalInterfaceSupported()) {
      flashSupport = MIME_TYPE_SUPPORT_NOT_READY;
    } else {
      flashSupport = MIME_TYPE_NOT_SUPPORTED;
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        public void execute() {
          movieUnsupported();
        }
      });
    }
  }

  public MimeTypeSupport getMimeTypeSupport(String mimeType) {
    switch (flashSupport) {
      case MIME_TYPE_SUPPORT_READY:
      case MIME_TYPE_SUPPORT_NOT_READY:
        return StringUtil.contains(FLASH_SUPPORTED_MIME_TYPES, mimeType) ? MIME_TYPE_SUPPORT_READY
            : MIME_TYPE_NOT_SUPPORTED;
      case MIME_TYPE_SUPPORT_UNKNOWN:
      case MIME_TYPE_NOT_SUPPORTED:
        return flashSupport;
      default:
        throw new RuntimeException("Unhandled flash support value " + flashSupport);
    }
  }

  public void playSound(int id) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callPlaySound(id);
    }
  }

  public void registerSound(FlashSound flashSound) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      doCreateSound(flashSound);
    } else {
      unitializedSoundList.add(flashSound);
    }
  }

  public void setBalance(int id, int balance) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callSetBalance(id, balance);
    }
  }

  public void setVolume(int id, int volume) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callSetVolume(id, volume);
    }
  }

  public void stopSound(int id) {
    if (flashSupport == MIME_TYPE_SUPPORT_READY) {
      callStopSound(id);
    }
  }

  protected void debug(String text) {
    System.out.println(text);
  }

  /**
   * Defer the actual work of a flash callback so that any exceptions can be caught by the browser
   * or the uncaught exception handler, rather than being swallow by flash.
   *
   * @param func the JavaScript function to call
   */
  protected void deferFlashCallback(final JavaScriptObject func) {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      public native void callFunc(JavaScriptObject func, VoicesMovie thiz) /*-{
		func.apply(thiz);
  }-*/;

      public void execute() {
        callFunc(func, VoicesMovie.this);
      }
    });
  }

  private native void callCreateSound(int id, String soundURL, boolean streaming) /*-{
		var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
		elem.createSound(id, soundURL, streaming);
  }-*/;

  private native void callPlaySound(int id) /*-{
		var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
		elem.playSound(id);
  }-*/;

  private native void callSetBalance(int id, int balance) /*-{
		var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
		elem.setBalance(id, balance);
  }-*/;

  private native void callSetVolume(int id, int volume) /*-{
		var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
		elem.setVolume(id, volume);
  }-*/;

  private native void callStopSound(int id) /*-{
		var elem = this.@com.allen_sauer.gwt.voices.client.ui.FlashMovie::element;
		elem.stopSound(id);
  }-*/;

  private void doCreateSound(FlashSound flashSound) {
    callCreateSound(flashSound.getSoundNumber(), flashSound.getUrl(), flashSound.isStreaming());
  }

  private native void installFlashCallbackHooks(String id) /*-{
    if ($doc.VoicesMovie === undefined) {
      $doc.VoicesMovie = {};
    }
    var self = this;
    $doc.VoicesMovie[id] = {};
    
    $doc.VoicesMovie[id].ready = function() {
      try {
        self.@com.allen_sauer.gwt.voices.client.ui.VoicesMovie::deferFlashCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(function() {
          this.@com.allen_sauer.gwt.voices.client.ui.VoicesMovie::movieReady()();
        });
        return true;
      } catch(e) {
        // tell flash since throwing error would get lost
        return "Exception: " + e.message + " / " + e.description;
      }
    }

    $doc.VoicesMovie[id].soundLoaded = function(id) {
      try {
        self.@com.allen_sauer.gwt.voices.client.ui.VoicesMovie::deferFlashCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(function() {
          @com.allen_sauer.gwt.voices.client.FlashSound::soundLoaded(I)(id);
        });
        return true;
      } catch(e) {
        // tell flash since throwing error would get lost
        return "Exception: " + e.message + " / " + e.description;
      }
    }

    $doc.VoicesMovie[id].playbackCompleted = function(id) {
      try {
        self.@com.allen_sauer.gwt.voices.client.ui.VoicesMovie::deferFlashCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(function() {
          @com.allen_sauer.gwt.voices.client.FlashSound::playbackCompleted(I)(id);
        });
        return true;
      } catch(e) {
        // tell flash since throwing error would get lost
        return "Exception: " + e.message + " / " + e.description;
      }
    }

    $doc.VoicesMovie[id].log = function(text) {
      self.@com.allen_sauer.gwt.voices.client.ui.VoicesMovie::debug(Ljava/lang/String;)("FLASH[" + id + "]: " + text);
    }
  }-*/;

  @SuppressWarnings("unused")
  private void movieReady() {
    flashSupport = MIME_TYPE_SUPPORT_READY;
    for (Iterator<FlashSound> iterator = unitializedSoundList.iterator(); iterator.hasNext();) {
      FlashSound flashSound = iterator.next();
      doCreateSound(flashSound);
      iterator.remove();
    }
  }

  private void movieUnsupported() {
    for (FlashSound flashSound : unitializedSoundList) {
      flashSound.setLoadState(LOAD_STATE_NOT_SUPPORTED);
      // Flash plug-in may become available later; do not call iterator.remove()
    }
  }

}
