package com.parimatch.navigation.screen

import android.os.Bundle

/**
 * The first parameter stands for the argument type, the second for the related information type.
 * Restrictions for the argument type are the same as for [Bundle].
 * todo : separate through inheritance
 * todo : make arg parameter constrained by Serializable
 */
public interface ScreenContract <A: Any, I: Any>

/**
 * [Reason](https://discuss.kotlinlang.org/t/allow-bounds-on-typealias-type-parameters/16028) why there are no more aliases.
 */
public interface BlankScreenContract : ScreenContract<Unit, Unit>
