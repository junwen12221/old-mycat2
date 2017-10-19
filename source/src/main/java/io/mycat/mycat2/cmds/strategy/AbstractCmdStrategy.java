package io.mycat.mycat2.cmds.strategy;

import java.util.HashMap;
import java.util.Map;

import io.mycat.mycat2.MySQLCommand;
import io.mycat.mycat2.MycatSession;
import io.mycat.mycat2.cmds.CmdStrategy;
import io.mycat.mycat2.cmds.DirectPassthrouhCmd;
import io.mycat.mycat2.sqlannotations.CacheResult;
import io.mycat.mycat2.sqlannotations.SQLAnnotation;
import io.mycat.mycat2.sqlparser.BufferSQLContext;
import io.mycat.mycat2.sqlparser.BufferSQLParser;
import io.mycat.mysql.packet.MySQLPacket;

public abstract class AbstractCmdStrategy implements CmdStrategy {
	
	/**
	 * 进行MySQL命令的处理的容器
	 */
	protected Map<Byte, MySQLCommand> MYCOMMANDMAP = new HashMap<>();
	
	/**
	 * 进行SQL命令的处理的容器
	 */
	protected Map<Byte, MySQLCommand> MYSQLCOMMANDMAP = new HashMap<>();
	
	private Map<Byte,SQLAnnotation> staticAnnontationMap = new HashMap<>();

	/**
	 * sqlparser
	 */
	protected BufferSQLParser parser = new BufferSQLParser();

	public AbstractCmdStrategy(){
		initMyCmdHandler();
		initMySqlCmdHandler();
		initStaticAnnotation();
	}
	
	private void initStaticAnnotation(){
		staticAnnontationMap.put(BufferSQLContext.ANNOTATION_SQL_CACHE,new CacheResult());
	}
	
	protected abstract void initMyCmdHandler();
	
	protected abstract void initMySqlCmdHandler();
	
	@Override
	final public void matchMySqlCommand(MycatSession session) {
		//初始化命令处理链
		session.getCmdChain().clear();
		
		MySQLCommand  command = null;
		if(MySQLPacket.COM_QUERY==(byte)session.curMSQLPackgInf.pkgType){
			/**
			 * sqlparser
			 */
			BufferSQLParser parser = new BufferSQLParser();
			int rowDataIndex = session.curMSQLPackgInf.startPos + MySQLPacket.packetHeaderSize +1 ;
			int length = session.curMSQLPackgInf.pkgLength -  MySQLPacket.packetHeaderSize - 1 ;
			parser.parse(session.proxyBuffer.getBuffer(), rowDataIndex, length, session.sqlContext);
			byte sqltype = session.sqlContext.getSQLType()!=0?session.sqlContext.getSQLType():session.sqlContext.getCurSQLType();
			command = MYSQLCOMMANDMAP.get(sqltype);
		}else{
			command = MYCOMMANDMAP.get((byte)session.curMSQLPackgInf.pkgType);
		}
		if(command==null){
			command = DirectPassthrouhCmd.INSTANCE;
		}

		/**
		 * 设置原始处理命令
		 */
		session.getCmdChain()
			   .setTarget(command)
			   .processAnnotation(session, staticAnnontationMap)
			   .build();
	}
}
