package com.cmy.parser;

import com.cmy.parser.bean.*;
import com.cmy.parser.bean.tabletype.ResTableEntry;
import com.cmy.parser.bean.tabletype.ResTableMapEntry;
import com.cmy.parser.bean.tabletype.ResTableType;
import com.cmy.parser.utils.ArscUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cmy on 2017/8/21
 */
public class ResTableEditor {


    public ResTable resTable;
    private File file;

    public ResTableEditor(String path) throws Exception {
        this(new File(path));
    }

    public ResTableEditor(File file) throws Exception {
        this.file = file;
        this.resTable = new ArscReader(file).read();
    }

    //modify some resources
    public void modifyResources(ResTablePackage resTablePackage, int packageID) {

        resTablePackage.resTableChunkList.forEach(resTableChunk -> {
            if (resTableChunk instanceof ResTableType) {
                ResTableType resTableType = (ResTableType) resTableChunk;
                byte typeID = resTableType.typeId;
                if(typeID == 02) {

                    int[] entryoffsets = resTableType.resTableEntryOffsets;
                    List<ResTableEntry> entryList = resTableType.resTableEntryList;
                    int[] targets = new int[]{0x54, 0x55};


//                    int index = -1;
//                    for(int i = 0; i < entryoffsets.length; i++){
//                        if(entryoffsets[i] != 0xffffffff){
//                            index++;
//                            if(i == targets[0] || i == targets[1]){
//                                System.out.println(new String(resTablePackage.keyStringPool.strings[entryList.get(index).index]));
//                                entryList.get(index).flags = 0x0004;
//                            }
//                        }
//                    }

                    for(int i = 0; i < targets.length; i++){
                        entryoffsets[targets[i]] = 0xffffffff;
                    }


//                    for(int i = 0; i < entryList.size(); i++){
//                        ResTableEntry resTableEntry = entryList.get(i);
//                        //if xx 对于要删除的资源，对应的offset设置成0xFFFFFFFF
//                        int index = resTableEntry.index;
//                        String typeName = new String(resTablePackage.typeStringPool.strings[resTableType.typeId-1]);
//                        String keyName = new String(resTablePackage.keyStringPool.strings[index]);
//                        if(keyName.contains("ic_share_qq") || keyName.contains("ic_share_sina")){
//                            System.out.println(String.format("%04x", packageID) +" " + String.format("%04x", typeID) + " " + typeName + " " + String.format("%04x", index) + " " +  String.format("%04x", i));
//                        }
//                        int nowID = (packageID << 24) | (typeID <<16) | i;
//                        if(nowID == 0x7f020054 || nowID == 0x7f020055) {
//                            System.out.println("=======liuyi: find the target resource====");
//                            //resTableType.resTableEntryOffsets[i] = 0xffff;
//                        }
//                    }

                }
            }
        });
    }

    //修改pp字段
    public void modifyPackageId(int pp) {
        ResTablePackage resTablePackage = resTable.resTablePackage;
        resTablePackage.packageHeader.packageId = pp;

        //保存一下资源项名称字符串

        resTablePackage.resTableChunkList.forEach(resTableChunk -> {
            if (resTableChunk instanceof ResTableType) {
                ResTableType resTableType = (ResTableType) resTableChunk;
                resTableType.resTableEntryList.forEach(resTableEntry -> {
                    if (resTableEntry instanceof ResTableMapEntry) {
                        ResTableMapEntry resTableMapEntry = (ResTableMapEntry) resTableEntry;
                        resTableMapEntry.parent = checkAndGetNewPP(resTableMapEntry.parent, pp);
                        resTableMapEntry.resTableMapList.forEach(resTableMap -> {
                            resTableMap.name = checkAndGetNewPP(resTableMap.name, pp);
                            resTableMap.value.data = checkAndGetNewPP(resTableMap.value.data, pp);
                        });
                    } else {
                        resTableEntry.resValue.data = checkAndGetNewPP(resTableEntry.resValue.data, pp);
                    }
                });
            }
        });
    }

    //修改library块
    public void modifyLibraryChunk(Map<Integer, String> ppMap) {
        int originalLibraryChunkSize = 0;
        ResTableChunk resTableChunk = resTable.resTablePackage.resTableChunkList.get(0);
        if (resTableChunk instanceof ResTableLibrary) {
            originalLibraryChunkSize = ((ResTableLibrary) resTableChunk).resChunkHeader.size;
            resTable.resTablePackage.resTableChunkList.remove(0);
        }

        if (ppMap == null || ppMap.size() == 0) {
            return;
        }

        ResTableLibrary resTableLibrary = new ResTableLibrary();
        resTableLibrary.resChunkHeader = new ResChunkHeader();
        resTableLibrary.resChunkHeader.type = ResTable.RES_TABLE_LIBRARY_TYPE;
        resTableLibrary.resChunkHeader.headerSize = 12;
        resTableLibrary.resTableLibraryEntries = new ArrayList<>();

        Set<Integer> keySet = ppMap.keySet();
        int count = keySet.size();
        resTableLibrary.count = count;
        resTableLibrary.resChunkHeader.size = 12 + count * 260;
        for (Integer pp : keySet) {
            ResTableLibrary.ResTableLibraryEntry entry = new ResTableLibrary.ResTableLibraryEntry();
            entry.packageId = pp;
            entry.packageName = ArscUtils.getUtf16String(ppMap.get(pp), 256);
            resTableLibrary.resTableLibraryEntries.add(entry);
        }
        resTable.resTablePackage.resTableChunkList.add(0, resTableLibrary);
        int incrementLength = (resTableLibrary.resChunkHeader.size - originalLibraryChunkSize);
        // 修改文件总长度
        resTable.resTableHeader.resChunkHeader.size += incrementLength;
        // 修改包长度
        resTable.resTablePackage.packageHeader.resChunkHeader.size += incrementLength;
    }

    private int checkAndGetNewPP(int value, int pp) {
        if (value >> 24 != 0x7f) {
            return value;
        }
        return (pp << 24) | (value & 0x00ffffff);
    }

    public void write() throws Exception {
        ArscWriter arscWriter = new ArscWriter(file);
        arscWriter.write(resTable);
    }

}
