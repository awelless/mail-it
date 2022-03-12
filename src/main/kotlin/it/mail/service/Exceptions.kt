package it.mail.service

class BadRequestException(message: String) : Exception(message)

class NotFoundException(message: String) : Exception(message)
