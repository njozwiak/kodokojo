/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.service.actor.user;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.kodokojo.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static akka.event.Logging.getLogger;

/**
 * Defined if a given user id is valid for creation, and check if username respect the db policy.
 */
public class UserEligibleActor extends AbstractActor {

    private final LoggingAdapter LOGGER = getLogger(getContext().system(), this);

    public static Props PROPS(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        return Props.create(UserEligibleActor.class, userRepository);
    }

    public UserEligibleActor(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("userRepository must be defined.");
        }
        receive(ReceiveBuilder.match(UserCreatorActor.UserCreateMsg.class , msg -> {
            boolean res = userRepository.identifierExpectedNewUser(msg.id);
            if (res) {
                res = userRepository.getUserByUsername(msg.username) == null;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("User {} is {}eligible", msg.getUsername(), res ? "" : "NOT ");
            }
            sender().tell(new UserEligibleResultMsg(res), self());
            getContext().stop(self());
        }).matchAny(this::unhandled).build());

    }

    public static class UserEligibleResultMsg {

        public final boolean isValid;

        public UserEligibleResultMsg(boolean isValid) {
            this.isValid = isValid;
        }
    }

}
