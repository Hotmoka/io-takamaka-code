/*
Copyright 2021 Fausto Spoto

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package io.takamaka.code.lang;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation stating that a class has the right to call
 * all methods of the Java library, but only during the initialization of the blockchain.
 * This annotation is typically used to insert some carefully designed classes
 * in blockchain, that perform a safe bridge between Takamaka smart contracts
 * and some Java library classes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ TYPE })
@Documented
public @interface WhiteListedDuringInitialization {
}