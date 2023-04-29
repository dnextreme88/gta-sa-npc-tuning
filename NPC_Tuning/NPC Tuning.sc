// ORIGINAL MOD: https://gist.github.com/JuniorDjjr/a68191d9f4b5fb642c7f658ca9c1b8bf
// You need: https://forum.mixmods.com.br/f141-gta3script-cleo/t5206-como-criar-scripts-com-cleoplus
SCRIPT_START
{
    LVAR_INT scplayer i j k iSeed iRandomLevel hVeh iType iWheelSet iSlot iTotalSlots pLabel iModel iVehModel bIsOffCity bUseFinalRandom bThisVehicleHasPartsInitialized iClass iFindProgress bOnRace
    LVAR_INT iFullTuningFinalPercent iLevels[5] bDisableNitro

    // Wheel sets
    CONST_INT ANY_SET         -1
    CONST_INT WHEEL_SET_TF    0 // Transfender
    CONST_INT WHEEL_SET_WAA   1 // Wheel Arch Angel
    CONST_INT WHEEL_SET_LLC   2 // Loco Low Co.
    CONST_INT WHEEL_SET_MORE  3 // More...

    // Upgrade types
    CONST_INT UPGRADE_HOOD        0
    CONST_INT UPGRADE_VENT        1
    CONST_INT UPGRADE_SPOIL       2
    CONST_INT UPGRADE_SIDESKIRT   3
    CONST_INT UPGRADE_BULLFRNT    4
    CONST_INT UPGRADE_BULLREAR    5
    CONST_INT UPGRADE_LIGHTS      6
    CONST_INT UPGRADE_ROOF        7
    CONST_INT UPGRADE_NITRO       8
    CONST_INT UPGRADE_HYDRAULICS  9
    CONST_INT UPGRADE_STEREO      10
    CONST_INT UPGRADE_UNKNOWN     11
    CONST_INT UPGRADE_WHEELS      12
    CONST_INT UPGRADE_EXHAUST     13
    CONST_INT UPGRADE_BUMPFRNT    14
    CONST_INT UPGRADE_BUMPREAR    15
    CONST_INT UPGRADE_MISC        16
    CONST_INT UPGRADE_ALLWHEELS   17

    IF NOT READ_INT_FROM_INI_FILE "CLEO\NPC Tuning.ini" "Settings" "Level1" (iLevels[0])
        iLevels[0] = 5
    ENDIF
    IF NOT READ_INT_FROM_INI_FILE "CLEO\NPC Tuning.ini" "Settings" "Level2" (iLevels[1])
        iLevels[1] = 10
    ENDIF
    IF NOT READ_INT_FROM_INI_FILE "CLEO\NPC Tuning.ini" "Settings" "Level3" (iLevels[2])
        iLevels[2] = 15
    ENDIF
    IF NOT READ_INT_FROM_INI_FILE "CLEO\NPC Tuning.ini" "Settings" "Level4" (iLevels[3])
        iLevels[3] = 20
    ENDIF
    IF NOT READ_INT_FROM_INI_FILE "CLEO\NPC Tuning.ini" "Settings" "Level5" (iLevels[4])
        iLevels[4] = 25
    ENDIF
    IF NOT READ_INT_FROM_INI_FILE "CLEO\NPC Tuning.ini" "Settings" "FullTuningFinalPercent" (iFullTuningFinalPercent)
        // iFullTuningFinalPercent = 60
        iFullTuningFinalPercent = 75
    ENDIF
    IF NOT READ_INT_FROM_INI_FILE "CLEO\NPC Tuning.ini" "Settings" "DisableNitro" (bDisableNitro)
        bDisableNitro = TRUE
    ENDIF

    CLEO_CALL StoreAllWheels 0 ()()

    WHILE TRUE
        WAIT 0
        iFindProgress = 0
        WHILE GET_ANY_CAR_NO_SAVE_RECURSIVE iFindProgress (iFindProgress hVeh)
            GOSUB ProcessCar
        ENDWHILE
    ENDWHILE

    // We can't process this on car create event, because we need to check driver and if the car is script controlled
    ProcessCar:
    // Note that we don't even manipulate the value, just the vars existence is enough in this case
    IF GET_EXTENDED_CAR_VAR hVeh AUTO 1 (i)
        RETURN
    ELSE
        INIT_EXTENDED_CAR_VARS hVeh AUTO 1
    ENDIF

    CLEO_CALL ReadGlobalVar 0 (121)(bOnRace) // Custom_Tournament_Flag

    IF IS_CAR_SCRIPT_CONTROLLED hVeh
    AND bOnRace = FALSE
        RETURN
    ENDIF

    GET_CAR_MODEL hVeh (iVehModel)

    GET_VEHICLE_SUBCLASS hVeh (i)
    IF NOT i = VEHICLE_SUBCLASS_AUTOMOBILE
        RETURN
    ENDIF

    GET_VEHICLE_CLASS hVeh (iClass)
    IF iClass > 3
    OR iClass < 0
        // put exception for some vehicles, because there is no sense
        IF NOT iVehModel = BOBCAT
        AND NOT iVehModel = WALTON
            RETURN
        ENDIF
    ENDIF

    // This will ignore uncommon cars, like bfinject and tractor
    GET_CAR_ANIMGROUP hVeh (i)
    IF NOT i = CAR_ANIMGROUP_STANDARD_CAR
    AND NOT i = CAR_ANIMGROUP_LOW_CAR
    AND NOT i = CAR_ANIMGROUP_CONVERTIBLE
        RETURN
    ENDIF

    // Only young drivers
    GET_DRIVER_OF_CAR hVeh (j)
    IF j > 0
        GET_CHAR_STAT_ID j (i)
        IF i > PEDSTAT_GANG10
            IF i = PEDSTAT_STREET_GUY
            OR i = PEDSTAT_STREET_GIRL
            OR i = PEDSTAT_GEEK_GIRL
            OR i = PEDSTAT_GEEK_GUY
            OR i = PEDSTAT_CRIMINAL
            OR i = PEDSTAT_BEACH_GUY
            OR i = PEDSTAT_BEACH_GIRL
                // Add exception for some peds, because there is no sense
                IF IS_CHAR_MODEL j BFOST
                    RETURN
                ENDIF
            ELSE
                RETURN
            ENDIF
        ENDIF
    ENDIF

    IF IS_CAR_OWNED_BY_PLAYER hVeh
        RETURN
    ENDIF

    IF iVehModel = SADLSHIT
    OR iVehModel = GLENSHIT
    OR iVehModel = CADDY
    OR iVehModel = MOWER
        RETURN
    ENDIF

    // Init parts list
    bThisVehicleHasPartsInitialized = FALSE

    IF IS_CAR_STREET_RACER hVeh
        iWheelSet = WHEEL_SET_WAA
    ELSE
        IF IS_CAR_LOW_RIDER hVeh
            iWheelSet = WHEEL_SET_LLC
        ELSE
            iWheelSet = WHEEL_SET_TF
        ENDIF
    ENDIF

    /*REPEAT 18 iType
        GOSUB InstallThisType
    ENDREPEAT
    RETURN*/

    GET_CITY_PLAYER_IS_IN 0 (i)
    IF i = 0 //countryside
    OR i = 4 //desert
        bIsOffCity = TRUE
    ELSE
        bIsOffCity = FALSE
    ENDIF

    GET_CAR_RANDOM_SEED hVeh (iSeed)

    GENERATE_RANDOM_INT_IN_RANGE_WITH_SEED iSeed 0 101 (iRandomLevel)

    // If executive, try random level more times, and use the lower one, so it becomes more rare, but keeps the high levels
    IF iClass = 3
        k = iSeed + 123456
        i = 0
        WHILE i < 3
            GENERATE_RANDOM_INT_IN_RANGE_WITH_SEED k 0 101 (i)
            IF i < iRandomLevel
                iRandomLevel = i
            ENDIF
            k += 123456
            ++i
        ENDWHILE
    ENDIF

    //iRandomLevel = 100

    bUseFinalRandom = TRUE

    IF iRandomLevel > 75 // full tuning, DEFAULT: IF iRandomLevel = 100
    AND iFullTuningFinalPercent > 0
        IF bIsOffCity = FALSE
            IF iRandomLevel > iFullTuningFinalPercent
                bUseFinalRandom = FALSE
                REPEAT 18 iType
                    GOSUB InstallThisType
                ENDREPEAT
                GET_CURRENT_VEHICLE_PAINTJOB hVeh (i)
                IF i <= 0
                    GET_NUM_AVAILABLE_PAINTJOBS hVeh (i)
                    IF i > 0
                        GENERATE_RANDOM_INT_IN_RANGE_WITH_SEED iSeed -1 i (i)
                        IF i >= 0
                            GIVE_VEHICLE_PAINTJOB hVeh i
                        ENDIF
                    ENDIF
                ENDIF
                IF IS_CAR_LOW_RIDER hVeh
                AND NOT DOES_CAR_HAVE_HYDRAULICS hVeh
                    SET_CAR_HYDRAULICS hVeh ON
                ENDIF

                // Street cars / Low riders only version
                // IF IS_CAR_STREET_RACER hVeh
                // OR IS_CAR_LOW_RIDER hVeh
                //     GET_CURRENT_VEHICLE_PAINTJOB hVeh (i)
                //     IF i <= 0
                //         GET_NUM_AVAILABLE_PAINTJOBS hVeh (i)
                //         IF i > 0
                //             GENERATE_RANDOM_INT_IN_RANGE_WITH_SEED iSeed -1 i (i)
                //             IF i >= 0
                //                 GIVE_VEHICLE_PAINTJOB hVeh i
                //             ENDIF
                //         ENDIF
                //     ENDIF
                //     IF IS_CAR_LOW_RIDER hVeh
                //     AND NOT DOES_CAR_HAVE_HYDRAULICS hVeh
                //         SET_CAR_HYDRAULICS hVeh ON
                //     ENDIF
                // ENDIF
            ENDIF
        ENDIF
        RETURN
    ENDIF

    IF IS_CAR_LOW_RIDER hVeh
    OR IS_CAR_STREET_RACER hVeh
        IF iRandomLevel < iLevels[0]
            RETURN
        ENDIF
    ELSE
        IF iRandomLevel < iLevels[1]
            RETURN
        ENDIF
    ENDIF
    iType = UPGRADE_ALLWHEELS
    GOSUB InstallThisType
    iType = UPGRADE_STEREO
    GOSUB InstallThisType
    IF IS_CAR_LOW_RIDER hVeh
        iType = UPGRADE_ROOF
        GOSUB InstallThisType
    ENDIF

    IF iRandomLevel < iLevels[2]
        RETURN
    ENDIF
    iType = UPGRADE_EXHAUST
    GOSUB InstallThisType
    IF IS_CAR_LOW_RIDER hVeh
        iType = UPGRADE_MISC
        GOSUB InstallThisType
    ELSE
        IF NOT IS_CAR_STREET_RACER hVeh
            iType = UPGRADE_SIDESKIRT
            GOSUB InstallThisType
        ENDIF
    ENDIF

    IF iRandomLevel < iLevels[3]
        RETURN
    ENDIF
    iType = UPGRADE_LIGHTS
    GOSUB InstallThisType
    iType = UPGRADE_SPOIL
    GOSUB InstallThisType
    // IF bIsOffCity = TRUE
    //     iType = UPGRADE_LIGHTS
    //     GOSUB InstallThisType
    // ELSE
    //     iType = UPGRADE_SPOIL
    //     GOSUB InstallThisType
    // ENDIF

    IF iRandomLevel < iLevels[4]
        RETURN
    ENDIF
    IF IS_CAR_STREET_RACER hVeh
        bUseFinalRandom = FALSE
        iType = UPGRADE_BUMPFRNT
        GOSUB InstallThisType
        iType = UPGRADE_BUMPREAR
        GOSUB InstallThisType
        iType = UPGRADE_SIDESKIRT
        GOSUB InstallThisType
        bUseFinalRandom = TRUE
    ELSE
        IF IS_CAR_LOW_RIDER hVeh
            bUseFinalRandom = FALSE
            iType = UPGRADE_BULLFRNT
            GOSUB InstallThisType
            iType = UPGRADE_BULLREAR
            GOSUB InstallThisType
            iType = UPGRADE_SIDESKIRT
            GOSUB InstallThisType
            bUseFinalRandom = TRUE
        ELSE
            iType = UPGRADE_HOOD
            GOSUB InstallThisType
        ENDIF
    ENDIF

    RETURN

    ///////////////////////////////////////////////////////////////////////////////////////////////

    InstallThisType:
    IF iType = UPGRADE_NITRO
    AND bDisableNitro = TRUE
        RETURN
    ENDIF

    IF bUseFinalRandom = TRUE
        i = iType * 5
        i += iSeed
        GENERATE_RANDOM_INT_IN_RANGE_WITH_SEED i 0 100 (i)
        IF i < 50 // another 50% chance to not create (this checks if the random number is odd or not)
            RETURN
        ENDIF
    ENDIF

    // To initialize parts for this car only if really required
    IF bThisVehicleHasPartsInitialized = FALSE
    AND NOT iType = UPGRADE_ALLWHEELS
        CLEO_CALL StoreAllUpgradesToCar 0 (iVehModel)()
        bThisVehicleHasPartsInitialized = TRUE
    ENDIF

    CLEO_CALL GetLabelFromType 0 (iType iWheelSet)(pLabel)

    CLEO_CALL CountSlots 0 (pLabel iType iWheelSet)(iTotalSlots)

    IF iTotalSlots > 0

        GENERATE_RANDOM_INT_IN_RANGE 0 iTotalSlots (iSlot)

        CLEO_CALL GetUpgradeIDFromMemorySlot 0 (pLabel iSlot)(iModel)

        IF iModel > 0

            IF iType = UPGRADE_ALLWHEELS
                IF bIsOffCity = FALSE
                    IF iModel = 1025 // wheel_or1
                        RETURN
                    ENDIF
                ENDIF
                IF iVehModel = WALTON
                    IF NOT iModel = 1025 // wheel_or1
                        RETURN
                    ENDIF
                ENDIF
                IF NOT iClass = 1 // poorfamily
                    IF iModel = 1078 // wheel_lr3
                    OR iModel = 1076 // wheel_lr4
                        IF NOT IS_CAR_LOW_RIDER hVeh
                            RETURN
                        ENDIF
                    ENDIF
                ENDIF
            ENDIF

            IF IS_MODEL_IN_CDIMAGE iModel

                // Fixes the most common crash caused by bad adapted cars
                IF iType = UPGRADE_EXHAUST
                    IF NOT DOES_CAR_HAVE_PART_NODE hVeh CAR_NODE_EXHAUST
                        RETURN
                    ENDIF
                ENDIF

                REQUEST_VEHICLE_MOD iModel
                LOAD_ALL_MODELS_NOW
                ADD_VEHICLE_MOD hVeh iModel (i)
                MARK_VEHICLE_MOD_AS_NO_LONGER_NEEDED iModel

            ENDIF

        ENDIF

    ENDIF
    RETURN

}
SCRIPT_END


{
    LVAR_INT var //In
    LVAR_INT value scriptSpace finalOffset

    ReadGlobalVar:
    READ_MEMORY 0x00468D5E 4 1 (scriptSpace)
    finalOffset = var * 4
    finalOffset += scriptSpace
    READ_MEMORY finalOffset 4 FALSE (value)
    CLEO_RETURN 0 (value)
}

{
    LVAR_INT nVehModelID // In
    LVAR_INT i nModel nVehModType pVehModelInfo pVehMods pLabel nMemorySlot offset value

    StoreAllUpgradesToCar:
    REPEAT 18 i
        IF NOT i = UPGRADE_WHEELS
        AND NOT i = UPGRADE_ALLWHEELS
            CLEO_CALL GetLabelFromType 0 (i -1)(pLabel)
            WRITE_MEMORY pLabel 16 0x00 FALSE
        ENDIF
    ENDREPEAT

    GET_MODEL_INFO nVehModelID (pVehModelInfo)
    pVehMods = pVehModelInfo + 0x2D6 // carMods

    REPEAT 18 i
        READ_MEMORY pVehMods 2 FALSE (nModel)
        IF nModel = 0xFFFF
            BREAK
        ENDIF

        // Store upgrade by type
        GET_VEHICLE_MOD_TYPE nModel (nVehModType)
        IF NOT nVehModType = UPGRADE_WHEELS
        AND NOT nVehModType = UPGRADE_ALLWHEELS
            CLEO_CALL GetLabelFromType 0 (nVehModType -1)(pLabel)

            nMemorySlot = 0
            WHILE nMemorySlot <= 8
                offset = nMemorySlot * 2
                offset += pLabel
                READ_MEMORY offset 2 FALSE (value)
                IF value = 0
                    WRITE_MEMORY offset 2 (nModel) FALSE
                    BREAK
                ENDIF
                nMemorySlot++
            ENDWHILE
        ENDIF

        pVehMods += 0x2
    ENDREPEAT
    CLEO_RETURN 0
}

{
    LVAR_INT label type nShop // In
    LVAR_INT totalSlots value i j
    CountSlots:
    IF type = UPGRADE_ALLWHEELS
    AND nShop < 0
        REPEAT 4 i
            CLEO_CALL GetLabelFromType 0 (17 i)(label)
            IF CLEO_CALL LabelNotEmpty 0 (label)
                totalSlots++
            ENDIF
        ENDREPEAT
        CLEO_RETURN 0 (totalSlots)
    ENDIF
    WHILE TRUE
        READ_MEMORY label 2 FALSE (value)
        IF value > 0
        AND NOT value = 0xFFFF
            totalSlots++
        ELSE
            BREAK
        ENDIF
        label += 0x2
    ENDWHILE
    CLEO_RETURN 0 (totalSlots)
}

{
    LVAR_INT label //In
    LVAR_INT value
    LabelNotEmpty:
    READ_MEMORY label 2 FALSE (value)
    IF value = 0xFFFF
        RETURN_FALSE
        CLEO_RETURN 0
    ENDIF
    IS_THING_GREATER_THAN_THING value 0
    CLEO_RETURN 0
}

{
    LVAR_INT label slot //In
    LVAR_INT id offset
    GetUpgradeIDFromMemorySlot:
    offset = slot * 2
    offset += label
    READ_MEMORY offset 2 FALSE (id)
    CLEO_RETURN 0 (id)
}


{
    LVAR_INT nVehModType nShop //In
    LVAR_INT pLabel

    GetLabelFromType:
    IF nShop >= 0
    AND nVehModType = UPGRADE_ALLWHEELS
        SWITCH nShop
            CASE WHEEL_SET_TF
                GET_LABEL_POINTER TMemory_Wheels_TF (pLabel)
                BREAK
            CASE WHEEL_SET_WAA
                GET_LABEL_POINTER TMemory_Wheels_WAA (pLabel)
                BREAK
            CASE WHEEL_SET_LLC
                GET_LABEL_POINTER TMemory_Wheels_LLC (pLabel)
                BREAK
            CASE WHEEL_SET_MORE
                GET_LABEL_POINTER TMemory_Wheels_MORE (pLabel)
                BREAK
            DEFAULT
                GET_LABEL_POINTER TMemory_Wheels_TF (pLabel)
                BREAK
        ENDSWITCH
        CLEO_RETURN 0 (pLabel)
    ENDIF
    IF nVehModType < 0
        CLEO_RETURN 0 (-1)
    ENDIF
    SWITCH nVehModType
        CASE UPGRADE_HOOD
            GET_LABEL_POINTER TMemory_HOOD (pLabel)
            BREAK
        CASE UPGRADE_VENT
            GET_LABEL_POINTER TMemory_VENT (pLabel)
            BREAK
        CASE UPGRADE_SPOIL
            GET_LABEL_POINTER TMemory_SPOIL (pLabel)
            BREAK
        CASE UPGRADE_SIDESKIRT
            GET_LABEL_POINTER TMemory_SIDESKIRT (pLabel)
            BREAK
        CASE UPGRADE_BULLFRNT
            GET_LABEL_POINTER TMemory_BULLFRNT (pLabel)
            BREAK
        CASE UPGRADE_BULLREAR
            GET_LABEL_POINTER TMemory_BULLREAR (pLabel)
            BREAK
        CASE UPGRADE_LIGHTS
            GET_LABEL_POINTER TMemory_LIGHTS (pLabel)
            BREAK
        CASE UPGRADE_ROOF
            GET_LABEL_POINTER TMemory_ROOF (pLabel)
            BREAK
        CASE UPGRADE_NITRO
            GET_LABEL_POINTER TMemory_NITRO (pLabel)
            BREAK
        CASE UPGRADE_HYDRAULICS
            GET_LABEL_POINTER TMemory_HYDRAULICS (pLabel)
            BREAK
        CASE UPGRADE_STEREO
            GET_LABEL_POINTER TMemory_STEREO (pLabel)
            BREAK
        CASE UPGRADE_WHEELS
            GET_LABEL_POINTER TMemory_WHEELS (pLabel)
            BREAK
        CASE UPGRADE_EXHAUST
            GET_LABEL_POINTER TMemory_EXHAUST (pLabel)
            BREAK
        CASE UPGRADE_BUMPFRNT
            GET_LABEL_POINTER TMemory_BUMPFRNT (pLabel)
            BREAK
        CASE UPGRADE_BUMPREAR
            GET_LABEL_POINTER TMemory_BUMPREAR (pLabel)
            BREAK
        CASE UPGRADE_MISC
            GET_LABEL_POINTER TMemory_MISC (pLabel)
            BREAK
        CASE UPGRADE_ALLWHEELS
            GET_LABEL_POINTER TMemory_ALLWHEELS (pLabel)
            BREAK
        DEFAULT
            GET_LABEL_POINTER TMemory_MISC (pLabel)
            BREAK
    ENDSWITCH
    CLEO_RETURN 0 (pLabel)
}

{
    LVAR_INT nTotal i pLabel wheel[15]

    StoreAllWheels:

    REPEAT 4 i
        CLEO_CALL GetLabelFromType 0 (17 i)(pLabel)
        CLEO_CALL StoreWheelsInSet 0 (i pLabel)()
    ENDREPEAT

    CLEO_RETURN 0
}

{
    LVAR_INT nSetID pLabel // In
    LVAR_INT nTotal x pSet wheelId

    StoreWheelsInSet:
    pSet = nSetID * 0x1E
    pSet = pSet + 0x00B4E3F8

    nSetID = nSetID * 2
    nSetID = nSetID + 0x00B4E470

    READ_MEMORY nSetID 2 FALSE nTotal

    IF NOT nTotal = 0
        x = 0
        WHILE x < nTotal
            READ_MEMORY pSet 2 FALSE wheelId
            WRITE_MEMORY pLabel 2 wheelId FALSE
            pSet += 0x2
            pLabel += 0x2
            x++
        ENDWHILE
    ENDIF
    CLEO_RETURN 0
}


TMemory_Start:
TMemory_HOOD:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_VENT:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_SPOIL:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_SIDESKIRT:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_BULLFRNT:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_BULLREAR:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_LIGHTS:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_ROOF:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_NITRO:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_HYDRAULICS:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_STEREO:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_WHEELS: //hardcoded
DUMP
00 00
ENDDUMP

TMemory_ALLWHEELS: //hardcoded
DUMP
00 01
ENDDUMP

TMemory_EXHAUST:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_BUMPFRNT:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_BUMPREAR:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_MISC:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
FF FF FF FF
ENDDUMP

TMemory_Wheels_TF:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //16
FF FF FF FF
ENDDUMP

TMemory_Wheels_WAA:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //16
FF FF FF FF
ENDDUMP

TMemory_Wheels_LLC:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //16
FF FF FF FF
ENDDUMP

TMemory_Wheels_MORE:
DUMP
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //8
00 00  00 00  00 00  00 00  00 00  00 00  00 00  00 00 //16
FF FF FF FF
ENDDUMP
