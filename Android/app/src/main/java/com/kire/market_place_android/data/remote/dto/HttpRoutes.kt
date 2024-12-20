package com.kire.market_place_android.data.remote.dto

object HttpRoutes {

    private const val BASE_URL = "http://192.168.148.135:8081/api/v1"

    //Authentication
    const val AUTH_LOGIN = "$BASE_URL/auth"
    const val AUTH_REGISTER = "$BASE_URL/auth/register"

    //User
    const val USER_INFO_BY_ID = "$BASE_URL/users/"
    const val CHANGE_PASSWORD = "$BASE_URL/users/password"
    const val CHANGE_CARD = "$BASE_URL/users/card"
    const val CHANGE_INFO_BY_ID = "$BASE_URL/users/"
    const val ALL_USERS = "$BASE_URL/users"

    //Product
    const val GET_ALL_PRODUCTS = "$BASE_URL/products/assortment"
    const val GET_ALL_AVAILABLE_CATEGORIES = "$BASE_URL/products/categories"
    const val ADD_PRODUCT = "$BASE_URL/products"
    const val DELETE_PRODUCT_BY_ID = "$BASE_URL/products/"

    //Order
    const val GET_ORDERS_BY_USER = "$BASE_URL/orders/user"
    const val CREATE_ORDER = "$BASE_URL/orders"

    //Admin
    const val GET_ALL_PICK_UP_POINTS = "$BASE_URL/points"
    const val CREATE_PICK_UP_POINT = "$BASE_URL/points"
    const val UPDATE_PICK_UP_POINT_BY_ID = "$BASE_URL/points/"
    const val DELETE_PICK_UP_POINT_BY_ID = "$BASE_URL/points/"

    //Manager
    const val GET_PICK_UP_POINT_BY_MANAGER_ID = "$BASE_URL/points/manager/"
    const val CONFIRM_ORDER_BY_ID = "$BASE_URL/orders/"
    const val GET_ORDERED_PRODUCTS_BY_ORDER_ID = "$BASE_URL/orders/"
}