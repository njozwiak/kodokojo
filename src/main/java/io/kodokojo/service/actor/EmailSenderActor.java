/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.EmailSender;

import java.util.Arrays;
import java.util.List;

import static akka.event.Logging.getLogger;

public class EmailSenderActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(EmailSender emailSender) {
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }
        return Props.create(EmailSenderActor.class, emailSender);
    }

    private final EmailSender emailSender;

    public EmailSenderActor(EmailSender emailSender) {
        if (emailSender == null) {
            throw new IllegalArgumentException("emailSender must be defined.");
        }
        this.emailSender = emailSender;
        receive(ReceiveBuilder.match(EmailSenderMsg.class, msg -> {
            try {
                emailSender.send(msg.to, msg.cc, msg.ci, msg.object, msg.content, msg.htmlContent);
            } catch (RuntimeException e) {
                LOGGER.error("Unable to sent email to '{}', with subject '{}'", Arrays.toString(msg.to.toArray()), msg.object, e);
                sender().tell(Futures.failed(e), self());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Email sent to '{}' with subject '{}'", Arrays.toString(msg.to.toArray()), msg.object);
            }
            getContext().stop(self());
        }).matchAny(this::unhandled).build());
    }

    public static class EmailSenderMsg {

        private final List<String> to;

        private final List<String> cc;

        private final List<String> ci;

        private final String object;

        private final String content;

        private final boolean htmlContent;

        public EmailSenderMsg(List<String> to, List<String> cc, List<String> ci, String object, String content, boolean htmlContent) {
            this.to = to;
            this.cc = cc;
            this.ci = ci;
            this.object = object;
            this.content = content;
            this.htmlContent = htmlContent;
        }

        public EmailSenderMsg(List<String> to, String object, String content) {
            this(to, null, null, object, content, true);
        }
    }

}
