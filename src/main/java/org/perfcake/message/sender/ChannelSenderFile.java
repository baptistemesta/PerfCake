/*
 * -----------------------------------------------------------------------\
 * PerfCake
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package org.perfcake.message.sender;

import org.perfcake.PerfCakeException;
import org.perfcake.message.Message;
import org.perfcake.reporting.MeasurementUnit;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Sender can send message through NIO FileChannel.
 *
 * @author Dominik Hanák <domin.hanak@gmail.com>
 * @author Martin Večera <marvenec@gmail.com>
 */
public class ChannelSenderFile extends ChannelSender {
   /**
    * Sender's fileChannel
    */
   private FileChannel fileChannel;

   @Override
   public void init() {
   }

   @Override
   public void preSend(Message message, Map<String, String> properties) throws Exception {
      super.preSend(message, properties);

      fileChannel = new RandomAccessFile(getTarget(), "rw").getChannel();

      if (!fileChannel.isOpen()) {
         throw new PerfCakeException("Cannot open a file channel to target: " + getTarget());
      }
   }

   @Override
   public Serializable doSend(Message message, Map<String, String> properties, MeasurementUnit mu) throws Exception {
      if (messageBuffer != null) {
         fileChannel.write(messageBuffer);

         if (awaitResponse) {
            if (responseBuffer != null) {
               fileChannel.read(responseBuffer);
               return new String(responseBuffer.array(), Charset.forName("UTF-8"));
            } else {
               throw new PerfCakeException("Cannot read response with automatic buffer size configuration for an empty message.");
            }
         }
      }
      return null;
   }

   @Override
   public void postSend(Message message) throws Exception {
      super.postSend(message);
      try {
         fileChannel.close();
      } catch (IOException e) {
         throw new PerfCakeException("Error while closing the file channel: ", e);
      }
   }
}
