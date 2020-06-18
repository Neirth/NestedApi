package io.neirth.nestedapi.Users.Templates;

public enum Country {
    // Country List Standard ISO 3166
    AF("Afghanistan"), AX("AlandIslands"), AL("Albania"), DZ("Algeria"), AS("AmericanSamoa"), AD("Andorra"),
    AO("Angola"), AI("Anguilla"), AQ("Antarctica"), AG("AntiguaAndBarbuda"), AR("Argentina"), AM("Armenia"),
    AW("Aruba"), AU("Australia"), AT("Austria"), AZ("Azerbaijan"), BS("Bahamas"), BH("Bahrain"), BD("Bangladesh"),
    BB("Barbados"), BY("Belarus"), BE("Belgium"), BZ("Belize"), BJ("Benin"), BM("Bermuda"), BT("Bhutan"), BO("Bolivia"),
    BA("BosniaAndHerzegovina"), BW("Botswana"), BV("BouvetIsland"), BR("Brazil"), IO("BritishIndianOceanTerritory"),
    BN("BruneiDarussalam"), BG("Bulgaria"), BF("BurkinaFaso"), BI("Burundi"), KH("Cambodia"), CM("Cameroon"), 
    CA("Canada"), CV("CapeVerde"), KY("CaymanIslands"), CF("CentralAfricanRepublic"), TD("Chad"), CL("Chile"), CN("China"),
    CX("ChristmasIsland"), CC("CocosKeelingIslands"), CO("Colombia"), KM("Comoros"), CG("Congo"), CD("CongoDemocraticRepublic"),
    CK("CookIslands"), CR("CostaRica"), CI("CoteDIvoire"), HR("Croatia"), CU("Cuba"), CY("Cyprus"), CZ("CzechRepublic"),
    DK("Denmark"), DJ("Djibouti"), DM("Dominica"), DO("DominicanRepublic"), EC("Ecuador"), EG("Egypt"), SV("ElSalvador"),
    GQ("EquatorialGuinea"), ER("Eritrea"), EE("Estonia"), ET("Ethiopia"), FK("FalklandIslands"), FO("FaroeIslands"),
    FJ("Fiji"), FI("Finland"), FR("France"), GF("FrenchGuiana"), PF("FrenchPolynesia"), TF("FrenchSouthernTerritories"),
    GA("Gabon"), GM("Gambia"), GE("Georgia"), DE("Germany"), GH("Ghana"), GI("Gibraltar"), GR("Greece"), GL("Greenland"),
    GD("Grenada"), GP("Guadeloupe"), GU("Guam"), GT("Guatemala"), GG("Guernsey"), GN("Guinea"), GW("GuineaBissau"), GY("Guyana"),
    HT("Haiti"), HM("HeardIslandMcdonaldIslands"), VA("HolySeeVaticanCityState"), HN("Honduras"), HK("HongKong"), HU("Hungary"),
    IS("Iceland"), IN("India"), ID("Indonesia"), IR("Iran"), IQ("Iraq"), IE("Ireland"), IM("IsleOfMan"), IL("Israel"), IT("Italy"),
    JM("Jamaica"), JP("Japan"), JE("Jersey"), JO("Jordan"), KZ("Kazakhstan"), KE("Kenya"), KI("Kiribati"), KR("Korea"), KW("Kuwait"),
    KG("Kyrgyzstan"), LA("LaoPeoplesDemocraticRepublic"), LV("Latvia"), LB("Lebanon"), LS("Lesotho"), LR("Liberia"), LY("LibyanArabJamahiriya"),
    LI("Liechtenstein"), LT("Lithuania"), LU("Luxembourg"), MO("Macao"), MK("Macedonia"), MG("Madagascar"), MW("Malawi"), MY("Malaysia"),
    MV("Maldives"), ML("Mali"), MT("Malta"), MH("MarshallIslands"), MQ("Martinique"), MR("Mauritania"), MU("Mauritius"), YT("Mayotte"),
    MX("Mexico"), FM("Micronesia"), MD("Moldova"), MC("Monaco"), MN("Mongolia"), ME("Montenegro"), MS("Montserrat"), MA("Morocco"),
    MZ("Mozambique"), MM("Myanmar"), NA("Namibia"), NR("Nauru"), NP("Nepal"), NL("Netherlands"), AN("NetherlandsAntilles"),
    NC("NewCaledonia"), NZ("NewZealand"), NI("Nicaragua"), NE("Niger"), NG("Nigeria"), NU("Niue"), NF("NorfolkIsland"),
    MP("NorthernMarianaIslands"), NO("Norway"), OM("Oman"), PK("Pakistan"), PW("Palau"), PS("PalestinianTerritory"), PA("Panama"), 
    PG("PapuaNewGuinea"), PY("Paraguay"), PE("Peru"), PH("Philippines"), PN("Pitcairn"), PL("Poland"), PT("Portugal"),
    PR("PuertoRico"), QA("Qatar"), RE("Reunion"), RO("Romania"), RU("RussianFederation"), RW("Rwanda"), BL("SaintBarthelemy"),
    SH("SaintHelena"), KN("SaintKittsAndNevis"), LC("SaintLucia"), MF("SaintMartin"), PM("SaintPierreAndMiquelon"), VC("SaintVincentAndGrenadines"),
    WS("Samoa"), SM("SanMarino"), ST("SaoTomeAndPrincipe"), SA("SaudiArabia"), SN("Senegal"), RS("Serbia"), SC("Seychelles"), SL("SierraLeone"),
    SG("Singapore"), SK("Slovakia"), SI("Slovenia"), SB("SolomonIslands"), SO("Somalia"), ZA("SouthAfrica"), GS("SouthGeorgiaAndSandwichIsl"),
    ES("Spain"), LK("SriLanka"), SD("Sudan"), SR("Suriname"), SJ("SvalbardAndJanMayen"), SZ("Swaziland"), SE("Sweden"), CH("Switzerland"),
    SY("SyrianArabRepublic"), TW("Taiwan"), TJ("Tajikistan"), TZ("Tanzania"), TH("Thailand"), TL("TimorLeste"), TG("Togo"), TK("Tokelau"),
    TO("Tonga"), TT("TrinidadAndTobago"), TN("Tunisia"), TR("Turkey"), TM("Turkmenistan"), TC("TurksAndCaicosIslands"), TV("Tuvalu"),
    UG("Uganda"), UA("Ukraine"), AE("UnitedArabEmirates"), GB("UnitedKingdom"), US("UnitedStates"), UM("UnitedStatesOutlyingIslands"),
    UY("Uruguay"), UZ("Uzbekistan"), VU("Vanuatu"), VE("Venezuela"), VN("VietNam"), VG("VirginIslandsBritish"), VI("VirginIslandsUS"),
    WF("WallisAndFutuna"), EH("WesternSahara"), YE("Yemen"), ZM("Zambia"), ZW("Zimbabw");

    private final String name;

    Country(String name) {
    this.name = name;
    }

    public String getCountryName() {
        return name;
    }
}