/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.api.pmml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="value")
@XmlAccessorType(XmlAccessType.FIELD)
public class IntegerFieldOutput extends AbstractOutput<Integer> {
    private Integer value;
    
    public IntegerFieldOutput() {
        super();
    }

    
    
    public IntegerFieldOutput(String correlationId, String name, String displayValue, Double weight, Integer value) {
        super(correlationId, name, displayValue, weight);
        this.value = value;
    }



    public IntegerFieldOutput(String correlationId, String segmentationId, String segmentId, String name,
            String displayValue, Double weight, Integer value) {
        super(correlationId, segmentationId, segmentId, name, displayValue, weight);
        this.value = value;
    }



    public IntegerFieldOutput(String correlationId, String segmentationId, String segmentId, String name, Integer value) {
        super(correlationId, segmentationId, segmentId, name);
        this.value = value;
    }



    public IntegerFieldOutput(String correlationId, String name, Integer value) {
        super(correlationId, name);
        this.value = value;
    }



    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IntegerFieldOutput other = (IntegerFieldOutput) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }



    @Override
    public String toString() {
        return "IntegerFieldOutput [correlationId=" + getCorrelationId() + ", segmentationId="
                + getSegmentationId() + ", segmentId=" + getSegmentId() + ", name=" + getName()
                + ", displayValue=" + getDisplayValue() + ", value=" + value + ", weight=" + weight + "]";
    }

}
