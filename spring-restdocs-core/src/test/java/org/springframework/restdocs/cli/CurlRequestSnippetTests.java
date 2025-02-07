/*
 * Copyright 2014-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.restdocs.cli;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.AbstractSnippetTests;
import org.springframework.restdocs.templates.TemplateFormat;
import org.springframework.util.Base64Utils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CurlRequestSnippet}.
 *
 * @author Andy Wilkinson
 * @author Yann Le Guern
 * @author Dmitriy Mayboroda
 * @author Jonathan Pearlin
 * @author Paul-Christian Volkmer
 * @author Tomasz Kopczynski
 */
@RunWith(Parameterized.class)
public class CurlRequestSnippetTests extends AbstractSnippetTests {

	private CommandFormatter commandFormatter = CliDocumentation.singleLineFormat();

	public CurlRequestSnippetTests(String name, TemplateFormat templateFormat) {
		super(name, templateFormat);
	}

	@Test
	public void getRequest() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X GET"));
	}

	@Test
	public void nonGetRequest() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo").method("POST").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X POST"));
	}

	@Test
	public void requestWithContent() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo").content("content").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X GET -d 'content'"));
	}

	@Test
	public void getRequestWithQueryString() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo?param=value").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo?param=value' -i -X GET"));
	}

	@Test
	public void getRequestWithQueryStringWithNoValue() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo?param").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo?param' -i -X GET"));
	}

	@Test
	public void postRequestWithQueryString() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo?param=value").method("POST").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo?param=value' -i -X POST"));
	}

	@Test
	public void postRequestWithQueryStringWithNoValue() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo?param").method("POST").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo?param' -i -X POST"));
	}

	@Test
	public void postRequestWithOneParameter() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(
				this.operationBuilder.request("http://localhost/foo").method("POST").content("k1=v1").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X POST -d 'k1=v1'"));
	}

	@Test
	public void postRequestWithOneParameterAndExplicitContentType() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE).method("POST")
				.content("k1=v1").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X POST -d 'k1=v1'"));
	}

	@Test
	public void postRequestWithOneParameterWithNoValue() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo").method("POST").content("k1=").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X POST -d 'k1='"));
	}

	@Test
	public void postRequestWithMultipleParameters() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.method("POST").content("k1=v1&k1=v1-bis&k2=v2").build());
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash")
				.withContent("$ curl 'http://localhost/foo' -i -X POST" + " -d 'k1=v1&k1=v1-bis&k2=v2'"));
	}

	@Test
	public void postRequestWithUrlEncodedParameter() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(
				this.operationBuilder.request("http://localhost/foo").method("POST").content("k1=a%26b").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X POST -d 'k1=a%26b'"));
	}

	@Test
	public void postRequestWithJsonData() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.method("POST").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.content("{\"a\":\"alpha\"}").build());
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash").withContent(
				"$ curl 'http://localhost/foo' -i -X POST -H 'Content-Type: application/json' -d '{\"a\":\"alpha\"}'"));
	}

	@Test
	public void putRequestWithOneParameter() throws IOException {
		new CurlRequestSnippet(this.commandFormatter)
				.document(this.operationBuilder.request("http://localhost/foo").method("PUT").content("k1=v1").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X PUT -d 'k1=v1'"));
	}

	@Test
	public void putRequestWithMultipleParameters() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.method("PUT").content("k1=v1&k1=v1-bis&k2=v2").build());
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash")
				.withContent("$ curl 'http://localhost/foo' -i -X PUT" + " -d 'k1=v1&k1=v1-bis&k2=v2'"));
	}

	@Test
	public void putRequestWithUrlEncodedParameter() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(
				this.operationBuilder.request("http://localhost/foo").method("PUT").content("k1=a%26b").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X PUT -d 'k1=a%26b'"));
	}

	@Test
	public void requestWithHeaders() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).header("a", "alpha").build());
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash").withContent(
				"$ curl 'http://localhost/foo' -i -X GET" + " -H 'Content-Type: application/json' -H 'a: alpha'"));
	}

	@Test
	public void requestWithHeadersMultiline() throws IOException {
		new CurlRequestSnippet(CliDocumentation.multiLineFormat()).document(this.operationBuilder
				.request("http://localhost/foo").header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.header("a", "alpha").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent(String.format("$ curl 'http://localhost/foo' -i -X GET \\%n"
						+ "    -H 'Content-Type: application/json' \\%n" + "    -H 'a: alpha'")));
	}

	@Test
	public void requestWithCookies() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.cookie("name1", "value1").cookie("name2", "value2").build());
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash")
				.withContent("$ curl 'http://localhost/foo' -i -X GET" + " --cookie 'name1=value1;name2=value2'"));
	}

	@Test
	public void multipartPostWithNoSubmittedFileName() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/upload")
				.method("POST").header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
				.part("metadata", "{\"description\": \"foo\"}".getBytes()).build());
		String expectedContent = "$ curl 'http://localhost/upload' -i -X POST -H "
				+ "'Content-Type: multipart/form-data' -F " + "'metadata={\"description\": \"foo\"}'";
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash").withContent(expectedContent));
	}

	@Test
	public void multipartPostWithContentType() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/upload")
				.method("POST").header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
				.part("image", new byte[0]).header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
				.submittedFileName("documents/images/example.png").build());
		String expectedContent = "$ curl 'http://localhost/upload' -i -X POST -H "
				+ "'Content-Type: multipart/form-data' -F " + "'image=@documents/images/example.png;type=image/png'";
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash").withContent(expectedContent));
	}

	@Test
	public void multipartPost() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/upload")
				.method("POST").header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE)
				.part("image", new byte[0]).submittedFileName("documents/images/example.png").build());
		String expectedContent = "$ curl 'http://localhost/upload' -i -X POST -H "
				+ "'Content-Type: multipart/form-data' -F " + "'image=@documents/images/example.png'";
		assertThat(this.generatedSnippets.curlRequest()).is(codeBlock("bash").withContent(expectedContent));
	}

	@Test
	public void basicAuthCredentialsAreSuppliedUsingUserOption() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.header(HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("user:secret".getBytes()))
				.build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -u 'user:secret' -X GET"));
	}

	@Test
	public void customAttributes() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(this.operationBuilder.request("http://localhost/foo")
				.header(HttpHeaders.HOST, "api.example.com")
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).header("a", "alpha").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo' -i -X GET -H 'Host: api.example.com'"
						+ " -H 'Content-Type: application/json' -H 'a: alpha'"));
	}

	@Test
	public void deleteWithQueryString() throws IOException {
		new CurlRequestSnippet(this.commandFormatter).document(
				this.operationBuilder.request("http://localhost/foo?a=alpha&b=bravo").method("DELETE").build());
		assertThat(this.generatedSnippets.curlRequest())
				.is(codeBlock("bash").withContent("$ curl 'http://localhost/foo?a=alpha&b=bravo' -i " + "-X DELETE"));
	}

}
