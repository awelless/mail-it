package it.mail.admin.client.security

import org.eclipse.microprofile.config.spi.Converter

class CharArrayConverter : Converter<CharArray> {

    override fun convert(value: String?) = value?.toCharArray()
}
