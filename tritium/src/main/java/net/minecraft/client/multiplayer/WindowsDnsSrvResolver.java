package net.minecraft.client.multiplayer;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

final class WindowsDnsSrvResolver {
    private static final int DNS_TYPE_SRV = 33;
    private static final int DNS_FREE_RECORD_LIST = 1;

    private WindowsDnsSrvResolver() {
    }

    static String[] resolve(String hostname) {
        String queryName = "_minecraft._tcp." + hostname;

        if (!queryName.endsWith(".")) {
            queryName += ".";
        }

        PointerByReference results = new PointerByReference();
        int status = DnsApi.INSTANCE.DnsQuery_W(
                new WString(queryName),
                DNS_TYPE_SRV,
                0,
                null,
                results,
                null
        );

        if (status != 0) {
            return defaultAddress(hostname);
        }

        Pointer record = results.getValue();

        try {
            while (record != null) {
                long typeOffset = (long) Native.POINTER_SIZE * 2;
                int type = Short.toUnsignedInt(record.getShort(typeOffset));

                if (type == DNS_TYPE_SRV) {
                    long dataOffset = typeOffset + 16;
                    Pointer targetPointer = record.getPointer(dataOffset);

                    if (targetPointer != null) {
                        String target = targetPointer.getWideString(0);
                        int port = Short.toUnsignedInt(record.getShort(dataOffset + Native.POINTER_SIZE + 4));

                        if (!target.isEmpty() && !target.equals(".") && port != 0) {
                            return new String[]{target, Integer.toString(port)};
                        }
                    }
                }

                record = record.getPointer(0);
            }
        } finally {
            Pointer firstRecord = results.getValue();

            if (firstRecord != null) {
                DnsApi.INSTANCE.DnsRecordListFree(firstRecord, DNS_FREE_RECORD_LIST);
            }
        }

        return defaultAddress(hostname);
    }

    private static String[] defaultAddress(String hostname) {
        return new String[]{hostname, Integer.toString(25565)};
    }

    private interface DnsApi extends StdCallLibrary {
        DnsApi INSTANCE = Native.load("Dnsapi", DnsApi.class, W32APIOptions.UNICODE_OPTIONS);

        int DnsQuery_W(WString name, int type, int options, Pointer extra,
                       PointerByReference queryResults, Pointer reserved);

        void DnsRecordListFree(Pointer recordList, int freeType);
    }
}
