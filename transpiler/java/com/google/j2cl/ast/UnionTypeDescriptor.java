/*
 * Copyright 2017 Google Inc.
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
package com.google.j2cl.ast;

import static java.util.stream.Collectors.joining;

import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.j2cl.ast.annotations.Visitable;
import com.google.j2cl.common.ThreadLocalInterner;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * A union type.
 *
 * <p>Union types can only be introduced in Java in a {@code try {...} catch(...) {....}}
 * expression, when the exception variable is defined with the Java 8 multi-exception syntax e.g.
 *
 * <pre>
 * <code>
 *
 *  try {
 *     ...
 *   } catch (A | B e) {
 *     ...
 *   }
 * </code>
 * </pre>
 */
@AutoValue
@Visitable
public abstract class UnionTypeDescriptor extends TypeDescriptor {

  public abstract ImmutableList<TypeDescriptor> getUnionTypeDescriptors();

  @Override
  @Memoized
  public boolean isNullable() {
    return getUnionTypeDescriptors().stream().anyMatch(TypeDescriptor::isNullable);
  }

  @Override
  @Memoized
  public DeclaredTypeDescriptor getRawTypeDescriptor() {
    DeclaredTypeDescriptor typeDescriptor =
        (DeclaredTypeDescriptor) getUnionTypeDescriptors().get(0).getRawTypeDescriptor();
    // Find the closest common ancestor of all the types in the union.
    while (typeDescriptor != null && !isAssignableTo(typeDescriptor)) {
      typeDescriptor = typeDescriptor.getSuperTypeDescriptor();
    }
    return typeDescriptor == null ? TypeDescriptors.get().javaLangObject : typeDescriptor;
  }

  @Override
  @Nullable
  public DeclaredTypeDescriptor getMetadataTypeDescriptor() {
    return null;
  }

  @Override
  public boolean isUnion() {
    return true;
  }

  @Override
  @Memoized
  public UnionTypeDescriptor unparameterizedTypeDescriptor() {
    return newBuilder()
        .setUnionTypeDescriptors(
            TypeDescriptors.toUnparameterizedTypeDescriptors(getUnionTypeDescriptors()))
        .build();
  }

  @Override
  public boolean isAssignableTo(TypeDescriptor that) {
    return getUnionTypeDescriptors()
        .stream()
        .allMatch(typeDescriptor -> typeDescriptor.isAssignableTo(that));
  }

  @Override
  public Set<TypeDescriptor> getAllTypeVariables() {
    return getUnionTypeDescriptors()
        .stream()
        .map(TypeDescriptor::getAllTypeVariables)
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public String getUniqueId() {
    return getUnionTypeDescriptors()
        .stream()
        .map(TypeDescriptor::getUniqueId)
        .collect(joining("|", "(", ")"));
  }

  @Override
  public UnionTypeDescriptor toNullable() {
    return this;
  }

  @Override
  public UnionTypeDescriptor toNonNullable() {
    return this;
  }

  @Override
  public boolean canBeReferencedExternally() {
    return false;
  }

  @Memoized
  @Override
  public Map<TypeDescriptor, TypeDescriptor> getSpecializedTypeArgumentByTypeParameters() {
    ImmutableMap.Builder<TypeDescriptor, TypeDescriptor> mapBuilder = ImmutableMap.builder();
    for (TypeDescriptor typeDescriptor : getUnionTypeDescriptors()) {
      mapBuilder.putAll(typeDescriptor.getSpecializedTypeArgumentByTypeParameters());
    }
    return mapBuilder.build();
  }

  @Override
  public UnionTypeDescriptor specializeTypeVariables(
      Function<TypeDescriptor, TypeDescriptor> replacementTypeArgumentByTypeVariable) {
    if (replacementTypeArgumentByTypeVariable == Function.<TypeDescriptor>identity()) {
      return this;
    }

    ImmutableList<TypeDescriptor> specializedUnionTypes =
        getUnionTypeDescriptors()
            .stream()
            .map(
                typeDescriptor ->
                    typeDescriptor.specializeTypeVariables(replacementTypeArgumentByTypeVariable))
            .collect(ImmutableList.toImmutableList());

    return newBuilder().setUnionTypeDescriptors(specializedUnionTypes).build();
  }

  @Override
  public Node accept(Processor processor) {
    return Visitor_UnionTypeDescriptor.visit(processor, this);
  }

  @Override
  abstract Builder toBuilder();

  public static Builder newBuilder() {
    return new AutoValue_UnionTypeDescriptor.Builder();
  }

  /** Builder for a UnionTypeDescriptor. */
  @AutoValue.Builder
  public abstract static class Builder extends TypeDescriptor.Builder {

    public abstract Builder setUnionTypeDescriptors(Iterable<TypeDescriptor> components);

    abstract UnionTypeDescriptor autoBuild();

    private static final ThreadLocalInterner<UnionTypeDescriptor> interner =
        new ThreadLocalInterner<>();

    public UnionTypeDescriptor build() {
      return interner.intern(autoBuild());
    }
  }
}
