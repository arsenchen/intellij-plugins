// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.cli

import com.intellij.codeInsight.lookup.CharFilter
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.TextFieldWithAutoCompletion
import com.intellij.ui.TextFieldWithAutoCompletionListProvider


class SchematicOptionsTextField(project: Project?,
                                options: List<Option>) : TextFieldWithAutoCompletion<Option>(
  project, SchematicOptionsCompletionProvider(options), false, null)


private class SchematicOptionsCompletionProvider(options: List<Option>) : TextFieldWithAutoCompletionListProvider<Option>(
  options) {

  override fun getLookupString(item: Option): String {
    return "--" + item.name
  }

  override fun setItems(variants: MutableCollection<Option>?) {
    super.setItems(variants?.filter { it -> it.isVisible })
  }

  override fun getTypeText(item: Option): String? {
    var result = item.type?.capitalize()
    if (item.enum.isNotEmpty()) {
      result += " (" + item.enum.joinToString("|") + ")"
    }
    return result
  }

  override fun acceptChar(c: Char): CharFilter.Result? {
    return if (c == '-') CharFilter.Result.ADD_TO_PREFIX else null
  }

  override fun compare(item1: Option, item2: Option): Int {
    return StringUtil.compare(item1.name, item2.name, false)
  }

  override fun createLookupBuilder(item: Option): LookupElementBuilder {
    return super.createLookupBuilder(item)
      .withTailText(if (item.description != null) "  " + item.description else null, true)
  }

}