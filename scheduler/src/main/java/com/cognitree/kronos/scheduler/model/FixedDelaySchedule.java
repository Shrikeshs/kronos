/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognitree.kronos.scheduler.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.cognitree.kronos.scheduler.model.Schedule.Type.fixed;

/**
 * Allows to schedule workflow at a fixed delay.
 * <p>
 * In fixed-delay execution, each execution is scheduled relative to
 * the actual execution time of the previous execution.  If an execution
 * is delayed for any reason (such as garbage collection or other
 * background activity), subsequent executions will be delayed as well
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FixedDelaySchedule extends Schedule {
    private Type type = fixed;
    private long intervalInMs;

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    public long getIntervalInMs() {
        return intervalInMs;
    }

    public void setIntervalInMs(long intervalInMs) {
        this.intervalInMs = intervalInMs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FixedDelaySchedule that = (FixedDelaySchedule) o;
        return intervalInMs == that.intervalInMs &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, intervalInMs);
    }

    @Override
    public String toString() {
        return "FixedDelaySchedule{" +
                "type=" + type +
                ", intervalInMs=" + intervalInMs +
                '}';
    }
}
