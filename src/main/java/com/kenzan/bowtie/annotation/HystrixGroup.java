/**
 * Copyright (C) 2015 Kenzan (labs@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/***
 * <p>
 * Runtime method annotation used to specify the Hystrix settings 
 * </p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HystrixGroup {
    
    /***
     * <p>
     * Hystrix groupKey value
     * </p>
     * 
     * @return Hystrix groupKey value
     */
    public String groupKey();
    
    /***
     * <p>
     * Hystrix commandKey value
     * </p>
     * 
     * @return Hystrix commandKey value
     */
    public String commandKey();
    
}
