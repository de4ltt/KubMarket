package com.kire.market_place_android.presentation.util

object Validator {
    fun validatePhone(phone: String) {
        require(phone.isNotEmpty()) {
            "Заполните поле номера телефона"
        }
        require(
            Regex("^(\\+)?((\\d{2,3}) ?\\d|\\d)(([ -]?\\d)|( ?(\\d{2,3}) ?)){5,12}\\d$").matches(
                phone
            )
        ) {
            "Неправильный номер телефона"
        }
        require(phone.length <= 20) {
            "Длина номера не должна превышать 20 символов"
        }
    }

    fun validateEmail(email: String) {
        require(email.isNotEmpty()) {
            "Заполните поле e-mail"
        }
        require(Regex("^[\\w_.]+@([\\w-]+\\.)+[a-zA-Z-]{2,4}\$").matches(email)) {
            "Неправильный e-mail"
        }
        require(email.length <= 50) {
            "Длина e-mail не должна превышать 50 символов"
        }
    }

    fun validateName(name: String) {
        require(name.isNotEmpty()) {
            "Введите имя"
        }
        require(Regex("^([А-Яа-я\\s]+|[A-Za-z\\s]+)\$").matches(name)) {
            "Неправильное имя"
        }
        require(name.length <= 75) {
            "Длина имени не должна превышать 75 символов"
        }
    }

    fun validatePassword(password: String) {
        require(password.isNotEmpty()) {
            "Введите пароль"
        }
        require(password.length >= 8) {
            "Длина пароля должна быть от 8 символов"
        }
        require(password.length <= 100) {
            "Длина пароля не должна превышать 100 символов"
        }
        require(
            Regex("^[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?`~]{8,100}\$").matches(
                password
            )
        ) {
            "Неправильный пароль"
        }
    }

    fun validatePasswordNovelty(currentPassword: String, newPassword: String) {
        require(currentPassword != newPassword) {
            "Новый пароль совпадает с текущим"
        }
    }

    fun validateRepeatedPassword(password: String, repeatedPassword: String) {
        validatePassword(repeatedPassword)

        require(password == repeatedPassword) {
            "Пароли не совпадают"
        }
    }

    fun validateManagerId(managerId: String) {
        require(managerId.isNotEmpty()) {
            "Заполните поле ID менеджера"
        }
        require(Regex("\\d+").matches(managerId)) {
            "ID должно состоять только из цифр"
        }
    }

    fun validatePickUpPointAddress(address: String) {
        require(address.isNotEmpty()) {
            "Заполните поле адреса пункта выдачи"
        }
    }

    fun validatePickUpPointToUpdateId(pickUpPointoUpdateId: String) {
        require(pickUpPointoUpdateId.isNotEmpty()) {
            "Заполните поле адреса пункта выдачи"
        }
    }

    fun validateNumber(number: String) {
        require(number.isNotEmpty()) {
            "Введите код заказа"
        }
        require(Regex("\\d+").matches(number)) {
            "Код должен состоять только из цифр"
        }
    }
}

enum class VProductType {
    ITEM_CATEGORY,
    ITEM_DESCRIPTION,
    ITEM_PRICE,
    ITEM_UNIT,
    ITEM_NAME,
    ITEM_QUANTITY
}

fun String.isProductValid(type: VProductType): Boolean =
    try {

        when (type) {
            VProductType.ITEM_CATEGORY,
            VProductType.ITEM_DESCRIPTION,
            VProductType.ITEM_NAME,
            VProductType.ITEM_UNIT -> Validator.validateName(this)

            VProductType.ITEM_PRICE,
            VProductType.ITEM_QUANTITY -> Validator.validateNumber(this)
        }

        true
    } catch (e: IllegalArgumentException) {
        false
    }