package com.cmy.parser;

import com.cmy.parser.bean.*;
import com.cmy.parser.bean.tabletype.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * Created by cmy on 2017/6/7
 */
public class ResTablePrinter {

    private ResTable resTable;

    public ResTablePrinter(ResTable resTable) {
        this.resTable = resTable;
    }

    public void printResTableHeader() {
        ResTableHeader resTableHeader = resTable.resTableHeader;
        println("ResTableHeader", resTableHeader.resChunkHeader);
        println("ResTableHeader Package Count: ", resTableHeader.packageCount);
    }

    public void printGlobalResStringPool() {
        printResStringPool(resTable.globalResStringPool);
    }

    public void printResTablePackage(boolean printList) {
        ResStringPool globalStringPool = resTable.globalResStringPool;
        ResTablePackage resTablePackage = resTable.resTablePackage;

        println("ResTablePackageHeader ", resTablePackage.packageHeader.resChunkHeader);
        println("ResTablePackageHeader packageId: ", resTablePackage.packageHeader.packageId);
        println("ResTablePackageHeader packageName: " + new String(resTablePackage.packageHeader.packageName));
        println("ResTablePackageHeader typeStringOffset: ", resTablePackage.packageHeader.typeStringOffset);
        println("ResTablePackageHeader lastPublicType: ", resTablePackage.packageHeader.lastPublicType);
        println("ResTablePackageHeader keyStringOffset: ", resTablePackage.packageHeader.keyStringOffset);
        println("ResTablePackageHeader lastPublicKey: ", resTablePackage.packageHeader.lastPublicKey);
        println("ResTablePackageHeader typeIdOffset: ", resTablePackage.packageHeader.typeIdOffset);

        printResStringPool(resTablePackage.typeStringPool);
        printResStringPool(resTablePackage.keyStringPool);

        if (printList) {
            List<ResTableChunk> list = resTablePackage.resTableChunkList;
            for (ResTableChunk resTableData : list) {
                short type = resTableData.resChunkHeader.type;
                switch (type) {
                    case ResTable.RES_TABLE_LIBRARY_TYPE:
                        println("RES_TABLE_LIBRARY_TYPE");
                        println((ResTableLibrary) resTableData);
                        break;
                    case ResTable.RES_TABLE_TYPE_SPEC_TYPE:
                        println("RES_TABLE_TYPE_SPEC_TYPE");
                        println((ResTableTypeSpec) resTableData);
                        break;
                    case ResTable.RES_TABLE_TYPE_TYPE:
                        println("RES_TABLE_TYPE_TYPE");
                        println(resTablePackage.packageHeader.packageId, globalStringPool, resTablePackage.keyStringPool, (ResTableType) resTableData);
                        break;
                }
            }
        }
    }

    private static void println(ResTableLibrary resTableLibrary) {
        System.out.println("========ResTableLibrary========");
        if (resTableLibrary.resTableLibraryEntries != null) {
            resTableLibrary.resTableLibraryEntries.forEach(resTableLibraryEntry -> {
                System.out.println("packageId:" + resTableLibraryEntry.packageId);
                System.out.println("packageName:" + new String(resTableLibraryEntry.packageName));
            });
        }
    }

    private static void println(ResTableTypeSpec resTableTypeSpec) {
        // 暂时没写这个打印
    }

    public static void printResStringPool(ResStringPool resStringPool) {
        println("");
        println("############ 开始 打印字符串池############");
        ResStringPool.ResStringPoolHeader resStringPoolHeader = resStringPool.resStringPoolHeader;
        println("ResStringPool ", resStringPoolHeader.resChunkHeader);
        println("ResStringPool stringCount: ", resStringPoolHeader.stringCount);
        println("ResStringPool styleCount: ", resStringPoolHeader.styleCount);
        println("ResStringPool flags: ", resStringPoolHeader.flags);
        println("ResStringPool stringsStart: ", resStringPoolHeader.stringsStart);
        println("ResStringPool stylesStart: ", resStringPoolHeader.stylesStart);

        println("=====字符串=====");
        println(resStringPool.strings);
        println("=====样式=====");
        println(resStringPool.styles);
        println("############ 结束 打印字符串池############");
        println("");
    }

    private void println(int pp, ResStringPool globalStringPool, ResStringPool keyStringPool, ResTableType resTableType) {
        List<ResTableEntry> list = resTableType.resTableEntryList;
        System.out.println("资源项目组数:" + resTableType.typeId + " " + resTableType.entryCount + " " + resTableType.resTableEntryOffsets.length);
        int len = list.size();
        //len = 0;
        for (int i = 0; i < len; i++) {
            ResTableEntry entry = list.get(i);

//            if (entry.flags == 0 || entry.flags == 1) {
//                continue;
//            }

            //System.out.println("打印flags:" + entry.flags);
            if((entry.flags & ResTableEntry.FLAG_WEAK) == 0)
                continue;

            if ((entry.flags & ResTableEntry.FLAG_COMPLEX) == 0) {
                println("------------------------------------------");
                println("普通类型:");
                println("ResTableType TypeId: ", resTableType.typeId);
                println("ResTableEntry size: ", entry.size);
                println("ResTableEntry flags: ", entry.flags);
                println("ResTableEntry index: ", entry.index);
                String keyName = new String(keyStringPool.strings[entry.index]);
                println("ResTableEntry keyName: " + keyName);
                println("");
                println(pp, resTableType.typeId, keyName, globalStringPool, entry.resValue);
                println("");
                println("-------------------");
                println("");
            } else {
                println("------------------------------------------");
                println("复杂类型:");
                println("ResTableType TypeId: ", resTableType.typeId);
                println("ResTableEntry size: ", entry.size);
                println("ResTableEntry flags: ", entry.flags);
                println("ResTableEntry index: ", entry.index);
                String keyName = new String(keyStringPool.strings[entry.index]);
                println("ResTableEntry keyName: " + keyName);
                ResTableMapEntry valueMapEntry = (ResTableMapEntry) entry;
                println("ResTableMapEntry parent: ", valueMapEntry.parent);
                println("ResTableMapEntry count: ", valueMapEntry.count);

                List<ResTableMap> resTableMapList = valueMapEntry.resTableMapList;
                for (ResTableMap resValueMap : resTableMapList) {
                    println("");
                    println("ResTableMap Name: " + Integer.toHexString(resValueMap.name));
                    ResValue resValue = resValueMap.value;
                    println(pp, resTableType.typeId, keyName, globalStringPool, resValue);
                    println("");
                }
                println("------------------------------------------");
                println("");
            }
        }
    }

    private static void println(int pp, int tt, String key, ResStringPool globalStringPool, ResValue resValue) {
        println("ResValue size: ", resValue.size);
        println("ResValue res0: ", resValue.res0);
        println("ResValue dataType: ", resValue.dataType);
        println("ResValue data: ", resValue.data);
        if (resValue.dataType == 3) {
            println(String.format("%s=%s", key, new String(globalStringPool.strings[resValue.data])));
        }
    }

    private static void println(String prefix, ResChunkHeader resChunkHeader) {
        println(prefix + "ResChunkHeader Type: ", resChunkHeader.type);
        println(prefix + "ResChunkHeader Header Size: ", resChunkHeader.headerSize);
        println(prefix + "ResChunkHeader Size: ", resChunkHeader.size);
    }

    private static void println(byte[][] array) {
        System.out.println("总共有:" + array.length);
        for (int i = 0; i < array.length; i++) {
            println(new String(array[i]));
        }
    }

    private static void println(CharSequence charSequence) {
        System.out.println(charSequence);
    }

    private static void println(CharSequence charSequence, int value) {
        System.out.println(charSequence + "0x" + Integer.toHexString(value));
    }

}
