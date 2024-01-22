package org.gotson.komga.infrastructure.xml

import org.gotson.komga.interfaces.api.opds.v1.dto.prefixToNamespace
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter

@Configuration
class MappingJackson2XmlHttpMessageConverterConfiguration {
  @Bean
  fun mappingJackson2XmlHttpMessageConverter(builder: Jackson2ObjectMapperBuilder) =
    MappingJackson2XmlHttpMessageConverter(
      builder.createXmlMapper(true).factory(NamespaceXmlFactory(prefixToNamespace = prefixToNamespace)).build(),
    )
}
