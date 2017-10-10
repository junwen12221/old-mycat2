package io.mycat.mycat2.tasks;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.mycat.mycat2.AbstractMySQLSession;
import io.mycat.mycat2.beans.MySQLPackageInf;
import io.mycat.mysql.packet.MySQLPacket;

/**
 * task处理结果集的模板类
 * <p>
 * Created by ynfeng on 2017/8/28.
 */
public abstract class BackendIOTaskWithResultSet<T extends AbstractMySQLSession> extends AbstractBackendIOTask<T> {
	
	private static Logger logger = LoggerFactory.getLogger(BackendIOTaskWithResultSet.class);
	
    protected ResultSetState curRSState = ResultSetState.RS_STATUS_COL_COUNT;

    @Override
    public void onSocketRead(T session) throws IOException {
    	
    	try {
    		if (!session.readFromChannel()){
    			return;
    		}
		}catch(IOException e){
			curRSState = ResultSetState.RS_STATUS_READ_ERROR;
			onRsFinish(session,false,e.getMessage());
			return;
		}
    	
        for (; ; ) {
            AbstractMySQLSession.CurrPacketType currPacketType = session.resolveMySQLPackage(session.proxyBuffer, session.curMSQLPackgInf, true);
            //因为是解析所以只处理整包
            if (currPacketType == AbstractMySQLSession.CurrPacketType.Full) {
                MySQLPackageInf curMQLPackgInf = session.curMSQLPackgInf;
                switch (curRSState) {
                    case RS_STATUS_COL_COUNT:
                        onRsColCount(session);
                        curRSState = ResultSetState.RS_STATUS_COL_DEF;
                        break;
                    case RS_STATUS_COL_DEF:
                        if (curMQLPackgInf.pkgType == MySQLPacket.EOF_PACKET) {
                            curRSState = ResultSetState.RS_STATUS_ROW;
                        } else {
                            onRsColDef(session);
                        }
                        break;
                    case RS_STATUS_ROW:
                        if (curMQLPackgInf.pkgType == MySQLPacket.EOF_PACKET) {
                            curRSState = ResultSetState.RS_STATUS_FINISH;
                            onRsFinish(session,true,null);
                        } else {
                            onRsRow(session);
                        }
                        break;
                }
            } else {
                break;
            }
        }
    }
    
    abstract void onRsColCount(T session);

    abstract void onRsColDef(T session);

    abstract void onRsRow(T session);

    abstract void onRsFinish(T session,boolean success,String msg) throws IOException;

    public enum ResultSetState {
        /**
         * 结果集第一个包
         */
        RS_STATUS_COL_COUNT,
        /**
         * 结果集列定义
         */
        RS_STATUS_COL_DEF,
        /**
         * 结果集行数据
         */
        RS_STATUS_ROW,
        /**
         * 结果集完成
         */
        RS_STATUS_FINISH,
        
        /**
         * 结果集网络读取错误
         */
        RS_STATUS_READ_ERROR,
        
        /**
         * 结果集网络写入错误
         */
        RS_STATUS_WRITE_ERROR;
    }
}
