/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.websocket.action;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import org.elasticsearch.common.inject.BindingAnnotation;
import org.elasticsearch.common.settings.Settings;

/**
 * Configuration for the pubsub index name.
 * 
 * @author Jörg Prante <joergprante@gmail.com>
 */

@BindingAnnotation
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface PubSubIndexName {

    static class Conf {
        public static final String DEFAULT_INDEX_NAME = "pubsub";

        public static String indexName(Settings settings) {
            return settings.get("pubsub.index_name", DEFAULT_INDEX_NAME);
        }
    }
}
