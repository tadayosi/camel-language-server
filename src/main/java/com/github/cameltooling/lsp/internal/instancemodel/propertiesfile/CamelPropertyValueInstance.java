/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.cameltooling.lsp.internal.instancemodel.propertiesfile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.catalog.CamelCatalog;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentItem;

import com.github.cameltooling.lsp.internal.completion.CamelComponentOptionValuesCompletionsFuture;
import com.github.cameltooling.lsp.internal.completion.CamelEndpointCompletionProcessor;
import com.github.cameltooling.lsp.internal.instancemodel.ILineRangeDefineable;
import com.github.cameltooling.lsp.internal.parser.CamelKafkaUtil;

/**
 * Represents one value in properties file.
 * For instance, with "camel.component.timer.delay=1000",
 * it is used to represents "1000"
 */
public class CamelPropertyValueInstance implements ILineRangeDefineable {

	private String camelPropertyValue;
	private CamelPropertyKeyInstance key;

	private TextDocumentItem textDocumentItem;

	public CamelPropertyValueInstance(String camelPropertyFileValue, CamelPropertyKeyInstance key, TextDocumentItem textDocumentItem) {
		this.camelPropertyValue = camelPropertyFileValue;
		this.key = key;
		this.textDocumentItem = textDocumentItem;
	}

	public CompletableFuture<List<CompletionItem>> getCompletions(Position position, CompletableFuture<CamelCatalog> camelCatalog) {
		if (new CamelKafkaUtil().isCamelURIForKafka(key.getCamelPropertyKey())) {
			return new CamelEndpointCompletionProcessor(textDocumentItem, camelCatalog).getCompletions(position);
		} else {
			String startFilter = camelPropertyValue.substring(0, position.getCharacter() - key.getEndposition() -1);
			return camelCatalog.thenApply(new CamelComponentOptionValuesCompletionsFuture(this, startFilter));
		}
	}

	public String getCamelPropertyFileValue() {
		return camelPropertyValue;
	}
	
	public CamelPropertyKeyInstance getKey() {
		return key;
	}

	@Override
	public int getLine() {
		return key.getLine();
	}

	@Override
	public int getStartPositionInLine() {
		return key.getEndposition() + 1;
	}

	@Override
	public int getEndPositionInLine() {
		return getStartPositionInLine() + (camelPropertyValue != null ? camelPropertyValue.length() : 0);
	}

}
