package org.gotson.komga.infrastructure.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class BracketParamsRequestWrapperTest {
  @Nested
  inner class ParameterNames {
    @Test
    fun `given parameters with and without brackets when getting parameter names then only the name without bracket is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param")
      request.setParameter("param[]")

      // when
      val filtered = BracketParamsRequestWrapper(request)

      // then
      assertThat(filtered.parameterNames.toList()).containsExactlyInAnyOrder("param")
    }

    @Test
    fun `given parameters with brackets when getting parameter names then only the name without bracket is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param[]")

      // when
      val filtered = BracketParamsRequestWrapper(request)

      // then
      assertThat(filtered.parameterNames.toList()).containsExactlyInAnyOrder("param")
    }

    @Test
    fun `given parameters without brackets when getting parameter names then only the name without bracket is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param")

      // when
      val filtered = BracketParamsRequestWrapper(request)

      // then
      assertThat(filtered.parameterNames.toList()).containsExactlyInAnyOrder("param")
    }

    @Test
    fun `given empty parameters when getting parameter names then it is empty`() {
      // given
      val request = MockHttpServletRequest()

      // when
      val filtered = BracketParamsRequestWrapper(request)

      // then
      assertThat(filtered.parameterNames.toList()).isEmpty()
    }
  }

  @Nested
  inner class ParameterValue {
    @Test
    fun `given parameters with and without brackets when getting parameter value then both values are joined to string`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param", "a")
      request.setParameter("param[]", "b")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val value = filtered.getParameter("param")
      val valueBracket = filtered.getParameter("param[]")

      // then
      assertThat(value).isEqualTo(valueBracket)
      assertThat(value).isEqualTo("a,b")
    }

    @Test
    fun `given parameters with brackets when getting parameter value single value is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param[]", "b")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val value = filtered.getParameter("param")
      val valueBracket = filtered.getParameter("param[]")

      // then
      assertThat(value).isEqualTo(valueBracket)
      assertThat(value).isEqualTo("b")
    }

    @Test
    fun `given parameters without brackets when getting parameter value single value is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param", "b")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val value = filtered.getParameter("param")
      val valueBracket = filtered.getParameter("param[]")

      // then
      assertThat(value).isEqualTo(valueBracket)
      assertThat(value).isEqualTo("b")
    }

    @Test
    fun `given empty parameters when getting parameter value then return null`() {
      // given
      val request = MockHttpServletRequest()

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val value = filtered.getParameter("param")

      // then
      assertThat(value).isNull()
    }
  }

  @Nested
  inner class ParameterValues {
    @Test
    fun `given parameters with and without brackets when getting parameter values then both values are returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param[]", "a")
      request.setParameter("param", "b")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val values = filtered.getParameterValues("param")
      val valuesBracket = filtered.getParameterValues("param[]")

      // then
      assertThat(values).isEqualTo(valuesBracket)
      assertThat(values).containsExactlyInAnyOrder("a", "b")
    }

    @Test
    fun `given parameters with brackets when getting parameter values then value is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param[]", "a")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val values = filtered.getParameterValues("param")
      val valuesBracket = filtered.getParameterValues("param[]")

      // then
      assertThat(values).isEqualTo(valuesBracket)
      assertThat(values).containsExactlyInAnyOrder("a")
    }

    @Test
    fun `given parameters without brackets when getting parameter values then value is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param", "a")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val values = filtered.getParameterValues("param")
      val valuesBracket = filtered.getParameterValues("param[]")

      // then
      assertThat(values).isEqualTo(valuesBracket)
      assertThat(values).containsExactlyInAnyOrder("a")
    }

    @Test
    fun `given empty parameters when getting parameter values then return null`() {
      // given
      val request = MockHttpServletRequest()

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val values = filtered.getParameterValues("param")

      // then
      assertThat(values).isNull()
    }
  }

  @Nested
  inner class ParameterMap {
    @Test
    fun `given parameters with and without brackets when getting parameter map then both values are returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param[]", "a")
      request.setParameter("param", "b")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val map = filtered.parameterMap

      // then
      assertThat(map.keys).hasSize(1)
      assertThat(map.keys).containsExactly("param")
      assertThat(map["param"]).containsExactlyInAnyOrder("a", "b")
    }

    @Test
    fun `given parameters with brackets when getting parameter map then key without bracket is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param[]", "a")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val map = filtered.parameterMap

      // then
      assertThat(map.keys).hasSize(1)
      assertThat(map.keys).containsExactly("param")
      assertThat(map["param"]).containsExactlyInAnyOrder("a")
    }

    @Test
    fun `given parameters without brackets when getting parameter map then key without bracket is returned`() {
      // given
      val request = MockHttpServletRequest()
      request.setParameter("param", "a")

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val map = filtered.parameterMap

      // then
      assertThat(map.keys).hasSize(1)
      assertThat(map.keys).containsExactly("param")
      assertThat(map["param"]).containsExactlyInAnyOrder("a")
    }

    @Test
    fun `given empty parameters when getting parameter map then empty map is returned`() {
      // given
      val request = MockHttpServletRequest()

      // when
      val filtered = BracketParamsRequestWrapper(request)
      val map = filtered.parameterMap

      // then
      assertThat(map).isEmpty()
    }
  }
}
