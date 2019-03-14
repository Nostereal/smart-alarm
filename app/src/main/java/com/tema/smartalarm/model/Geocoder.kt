package com.tema.smartalarm.model

data class Geocoder(
    val response: Response
)

data class Response(
    val GeoObjectCollection: GeoObjectCollection
)

data class GeoObjectCollection(
    val featureMember: List<FeatureMember>,
    val metaDataProperty: MetaDataProperty
)

data class FeatureMember(
    val GeoObject: GeoObject
)

data class GeoObject(
    val Point: Point,
    val boundedBy: BoundedBy,
    val description: String,
    val metaDataProperty: MetaDataProperty,
    val name: String
)

data class Point(
    val pos: String
)

data class MetaDataProperty(
    val GeocoderMetaData: GeocoderMetaData
)

data class GeocoderMetaData(
    val Address: Address,
    val AddressDetails: AddressDetails,
    val kind: String,
    val precision: String,
    val text: String
)

data class AddressDetails(
    val Country: Country
)

data class Country(
    val AddressLine: String,
    val CountryName: String,
    val CountryNameCode: String,
    val Locality: Locality
)

data class Locality(
    val LocalityName: String,
    val Premise: Premise
)

data class Premise(
    val PremiseNumber: String
)

data class Address(
    val Components: List<Component>,
    val country_code: String,
    val formatted: String
)

data class Component(
    val kind: String,
    val name: String
)

data class BoundedBy(
    val Envelope: Envelope
)

data class Envelope(
    val lowerCorner: String,
    val upperCorner: String
)

data class MetaDataPropertyX(
    val GeocoderResponseMetaData: GeocoderResponseMetaData
)

data class GeocoderResponseMetaData(
    val found: String,
    val request: String,
    val results: String
)