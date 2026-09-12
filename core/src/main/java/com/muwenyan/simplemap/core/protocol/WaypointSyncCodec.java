package com.muwenyan.simplemap.core.protocol;

import com.muwenyan.simplemap.core.navigation.Waypoint;
import com.muwenyan.simplemap.core.waypoint.WaypointCodec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class WaypointSyncCodec {
    private WaypointSyncCodec() { }

    public static byte[] encode(WaypointSyncMessage message) throws ProtocolException {
        try {
            byte[] waypoints = WaypointCodec.encode(message.waypoints());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream(16 + waypoints.length);
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeLong(message.revision());
            out.writeInt(waypoints.length);
            out.write(waypoints);
            out.flush();
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot encode waypoint sync", exception);
        }
    }

    public static WaypointSyncMessage decode(byte[] encoded) throws ProtocolException {
        if (encoded == null || encoded.length < 12 || encoded.length > FrameCodec.MAX_BODY_BYTES) {
            throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid waypoint sync");
        }
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(encoded));
            long revision = in.readLong();
            int length = in.readInt();
            if (length < 1 || length > FrameCodec.MAX_BODY_BYTES || length != in.available()) {
                throw new ProtocolException(ProtocolErrorCode.INVALID_LENGTH, "invalid waypoint sync payload");
            }
            return new WaypointSyncMessage(revision, WaypointCodec.decode(in.readNBytes(length)));
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof ProtocolException protocolException) throw protocolException;
            throw new ProtocolException(ProtocolErrorCode.MALFORMED_BODY, "cannot decode waypoint sync", exception);
        }
    }
}
