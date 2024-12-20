package com.kire.market_place_android.presentation.navigation.util

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kire.market_place_android.presentation.constant.Strings
import com.kire.market_place_android.presentation.ui.screen.destinations.AdminPanelItemsScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.AdminPanelPickUpScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.AdminPanelScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.AdminPanelUsersScreenDestination
//import com.kire.market_place_android.presentation.ui.screen.destinations.FavouritesScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.ManagerScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.OrderScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.ProfileScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.ShoppingCartScreenDestination
import com.kire.market_place_android.presentation.ui.screen.destinations.ShoppingScreenDestination
import com.kire.test.R
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

/**
 * Доступные, в зависимости от роли, экраны
 *
 * @author Michael Gontarev (KiREHwYE)*/
sealed interface AppDestinations {
    enum class BottomBarDestinations (
        val direction: DirectionDestinationSpec,
        @DrawableRes val iconTop: Int?,
        @DrawableRes val iconBottom: Int?,
        val label: String,
        @DrawableRes val additionalButton: Int?
    ) : AppDestinations {

        SHOPPING(ShoppingScreenDestination, R.drawable.location, R.drawable.shopping_bottom_bar, Strings.HOME_SCREEN_LABEL, null),
//        FAVOURITE(FavouritesScreenDestination, R.drawable.favourite_top_bar, R.drawable.favourite_bottom_bar, Strings.FAVOURITE_SCREEN_LABEL, null),
        PROFILE(ProfileScreenDestination, R.drawable.profile_top_bar, R.drawable.profile_bottom_bar, Strings.PROFILE_SCREEN_LABEL, R.drawable.exit),
        ADMIN_PANEL(AdminPanelScreenDestination, null, R.drawable.admin_panel_icon, Strings.MANAGER_SCREEN_LABEL, null),
        MANAGER(ManagerScreenDestination, R.drawable.manager_top_bottom_bars, R.drawable.manager_top_bottom_bars, Strings.MANAGER_SCREEN_LABEL, null),
        SHOPPING_CART(ShoppingCartScreenDestination, R.drawable.shopping_cart_top_bar, R.drawable.shopping_cart_bottom_bar, Strings.SHOPPING_CART_SCREEN_LABEL, null)
    }

    enum class AdminDestinations (
        val direction: DirectionDestinationSpec,
        @DrawableRes val iconTop: Int?,
        @DrawableRes val iconBottom: Int?,
        val label: String,
        @DrawableRes val plusButton: Int?
    ) : AppDestinations {

        ADMIN_PANEL_ITEMS(AdminPanelItemsScreenDestination, R.drawable.admin_panel_icon, null, Strings.ITEMS, R.drawable.plus_bold),
        ADMIN_PANEL_USERS(AdminPanelUsersScreenDestination, R.drawable.admin_panel_icon, null, Strings.USERS, null),
        ADMIN_PANEL_PICK_UP(AdminPanelPickUpScreenDestination, R.drawable.admin_panel_icon, null, Strings.PICK_UP_POINTS, R.drawable.plus_bold ),
    }

    enum class ManagerDestinations (
        val direction: DirectionDestinationSpec,
        @DrawableRes val iconTop: Int?,
        @DrawableRes val iconBottom: Int?,
        val label: String,
        @DrawableRes val plusButton: Int?
    ) : AppDestinations {

        ORDER(OrderScreenDestination, R.drawable.arrow_back_top_bar, null, Strings.ORDER_TITLE, null)
    }
}


fun AppDestinations.getDirection() : DirectionDestinationSpec {
    return when(this) {
        is AppDestinations.BottomBarDestinations -> this.direction
        is AppDestinations.AdminDestinations -> this.direction
        is AppDestinations.ManagerDestinations -> this.direction
    }
}

fun AppDestinations.getLabel() : String {
    return when(this) {
        is AppDestinations.BottomBarDestinations -> this.label
        is AppDestinations.AdminDestinations -> this.label
        is AppDestinations.ManagerDestinations -> this.label
    }
}

fun AppDestinations.getIconTop() : Int? {
    return when(this) {
        is AppDestinations.BottomBarDestinations -> this.iconTop
        is AppDestinations.AdminDestinations -> this.iconTop
        is AppDestinations.ManagerDestinations -> this.iconTop
    }
}

fun AppDestinations.getPlusButton() : Int? {
    return when(this) {
        is AppDestinations.BottomBarDestinations -> this.additionalButton
        is AppDestinations.AdminDestinations -> this.plusButton
        is AppDestinations.ManagerDestinations -> this.plusButton
    }
}



