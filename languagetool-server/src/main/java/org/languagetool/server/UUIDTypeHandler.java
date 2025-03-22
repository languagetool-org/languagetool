package org.languagetool.server;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.BaseTypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.nio.ByteBuffer;

@MappedJdbcTypes(JdbcType.BINARY)
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
        UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setBytes(i, convertUUIDToBytes(parameter));
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName)
        throws SQLException {
        byte[] bytes = rs.getBytes(columnName);
        return convertBytesToUUID(bytes);
    }


    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex)
        throws SQLException {
        byte[] bytes = rs.getBytes(columnIndex);
        return convertBytesToUUID(bytes);
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex)
        throws SQLException {
        byte[] bytes = cs.getBytes(columnIndex);
        return convertBytesToUUID(bytes);
    }

    private byte[] convertUUIDToBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

    private UUID convertBytesToUUID(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long mostSigBits = byteBuffer.getLong();
        long leastSigBits = byteBuffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }
}
