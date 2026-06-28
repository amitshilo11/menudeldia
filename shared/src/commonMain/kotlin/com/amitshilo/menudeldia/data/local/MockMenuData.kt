package com.amitshilo.menudeldia.data.local

import com.amitshilo.menudeldia.domain.model.Dish
import com.amitshilo.menudeldia.domain.model.Menu
import kotlinx.datetime.LocalDate

private val mockDate = LocalDate(2026, 6, 10)

private fun menu(
    restaurantId: String,
    firsts: List<String>,
    seconds: List<String>,
    desserts: List<String>,
    price: Double,
    notes: String? = null,
) = Menu(
    id = "menu-$restaurantId",
    restaurantId = restaurantId,
    date = mockDate,
    price = price,
    currency = "EUR",
    firsts = firsts.map { Dish(it) },
    seconds = seconds.map { Dish(it) },
    desserts = desserts.map { Dish(it) },
    notes = notes,
)

val mockMenus: Map<String, Menu> = mapOf(
    "r1" to menu(
        "r1",
        firsts = listOf("Ensalada mixta", "Sopa de fideos", "Croquetas caseras"),
        seconds = listOf("Pollo al ajillo", "Merluza a la romana", "Filete de ternera"),
        desserts = listOf("Flan casero", "Fruta del tiempo"),
        price = 12.50,
    ),
    "r2" to menu(
        "r2",
        firsts = listOf("Gazpacho andaluz", "Macarrones con tomate", "Ensalada de la casa"),
        seconds = listOf("Bacalao a la llauna", "Conejo al romero", "Tortilla de patatas"),
        desserts = listOf("Crème brûlée", "Helado del día"),
        price = 13.00,
    ),
    "r3" to menu(
        "r3",
        firsts = listOf(
            "Crema de verduras",
            "Paella de verduras",
            "Pimientos del piquillo rellenos"
        ),
        seconds = listOf("Lubina al horno", "Magret de pato", "Hamburguesa casera"),
        desserts = listOf("Mousse de chocolate", "Fruta"),
        price = 14.00,
    ),
    "r4" to menu(
        "r4",
        firsts = listOf("Lentejas guisadas", "Ensaladilla rusa", "Canelones de espinacas"),
        seconds = listOf("Calamares a la romana", "Pollo asado", "Costillas de cerdo"),
        desserts = listOf("Tiramisú", "Yogur natural"),
        price = 11.50,
    ),
    "r5" to menu(
        "r5",
        firsts = listOf("Sopa de cebolla", "Espárragos con mahonesa", "Arroz tres delicias"),
        seconds = listOf("Salmón al horno", "Entrecot a la plancha", "Muslitos de pollo"),
        desserts = listOf("Tarta del día", "Macedonia de frutas"),
        price = 13.50,
    ),
    "r6" to menu(
        "r6",
        firsts = listOf("Fabada asturiana", "Ensalada caprese", "Champiñones al ajillo"),
        seconds = listOf("Rape con patatas", "Ternera guisada", "Croquetas de jamón"),
        desserts = listOf("Panna cotta", "Helado"),
        price = 12.00,
    ),
    "r7" to menu(
        "r7",
        firsts = listOf("Cocido madrileño", "Carpaccio de calabacín", "Almejas a la marinera"),
        seconds = listOf("Dorada al horno", "Secreto ibérico", "Revuelto de gambas"),
        desserts = listOf("Brownie con helado", "Fruta del tiempo"),
        price = 14.50,
    ),
    "r8" to menu(
        "r8",
        firsts = listOf("Gazpacho de remolacha", "Pulpo a la gallega", "Verduras salteadas"),
        seconds = listOf("Mero a la plancha", "Carrillera de cerdo", "Escalope milanesa"),
        desserts = listOf("Coulant de chocolate", "Sorbete de limón"),
        price = 13.00,
    ),
    "r9" to menu(
        "r9",
        firsts = listOf("Sopa castellana", "Ensalada niçoise", "Patatas bravas"),
        seconds = listOf("Pez espada a la plancha", "Pollo en pepitoria", "Berberechos al vapor"),
        desserts = listOf("Tocino de cielo", "Fruta"),
        price = 10.50,
    ),
    "r10" to menu(
        "r10",
        firsts = listOf("Crema de calabaza", "Boquerones en vinagre", "Garbanzos con espinacas"),
        seconds = listOf("Rodaballo al horno", "Rabo de toro", "Pechuga a la plancha"),
        desserts = listOf("Natillas", "Tarta de queso"),
        price = 11.00,
    ),
    "r11" to menu(
        "r11",
        firsts = listOf("Escarola amb anxoves", "Caldo de cocido", "Pimientos asados con atún"),
        seconds = listOf("Bacallà esqueixat", "Pollastre rostit", "Llom de porc a la brasa"),
        desserts = listOf("Crema catalana", "Fruita del tiempo"),
        price = 12.50,
    ),
    "r12" to menu(
        "r12",
        firsts = listOf("Gazpacho del día", "Judías verdes con jamón", "Arroz negro con alioli"),
        seconds = listOf("Calamar farcit", "Filete de ternera con patatas", "Rap amb gambes"),
        desserts = listOf("Flan de huevo", "Helado casero"),
        price = 13.50,
    ),
    "r13" to menu(
        "r13",
        firsts = listOf("Caldo de peix", "Escopinyes al vapor", "Amanida de mar"),
        seconds = listOf("Musclos a la marinera", "Calamars fregits", "Dorada a la sal"),
        desserts = listOf("Pastís de crema", "Fruita de temporada"),
        price = 11.50,
    ),
    "r14" to menu(
        "r14",
        firsts = listOf("Amanida de temporada", "Sopa de ceba gratinada", "Patates braves"),
        seconds = listOf("Tonyina encebollada", "Mandonguilles amb salsa", "Croquetes de pernil"),
        desserts = listOf("Pastís de formatge", "Iogurt amb mel"),
        price = 12.00,
    ),
    "r15" to menu(
        "r15",
        firsts = listOf("Escalivada amb formatge", "Brou de gallina", "Mongetes del ganxet"),
        seconds = listOf("Bacallà a la brasa", "Confit de canard", "Vedella guisada"),
        desserts = listOf("Mel i mató", "Crema de la casa"),
        price = 14.00,
        notes = "Productes 100% km0 i ecològics",
    ),
    "r16" to menu(
        "r16",
        firsts = listOf("Caldo de galets", "Esqueixada de bacallà", "Amanida verda"),
        seconds = listOf("Pollastre a l'ast", "Botifarra amb mongetes", "Rap al forn"),
        desserts = listOf("Crema catalana", "Formatge amb melmelada"),
        price = 14.50,
    ),
    "r17" to menu(
        "r17",
        firsts = listOf(
            "Sopa de fideus amb marisc",
            "Pop amb patates i vinagreta",
            "Escopinyes al vapor"
        ),
        seconds = listOf("Suquet de peix", "Calamar a la planxa amb arròs", "Gambes a l'ajillo"),
        desserts = listOf("Pastís de llimona", "Gelat artesà"),
        price = 15.00,
    ),
    "r18" to menu(
        "r18",
        firsts = listOf("Amanida de ruca i parmesà", "Sopa de tomàquet", "Ou ferrat amb bolets"),
        seconds = listOf("Entrecot a la brasa", "Truita de verdures", "Filet de porc amb figues"),
        desserts = listOf("Mousse de xocolata", "Fruita de temporada"),
        price = 12.50,
    ),
)
