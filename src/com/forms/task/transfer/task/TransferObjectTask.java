package com.forms.task.transfer.task;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import com.forms.platform.core.database.jndi.IJndi;
import com.forms.platform.core.logger.CommonLogger;
import com.forms.task.transfer.model.ITransferModel;

/**
 * Copy Right Information : Forms Syntron <br>
 * Project : 数据分析平台迁移项目 <br>
 * Description : 对象迁移任务<br>
 * Author : OuLinhai <br>
 * Version : 1.0.0 <br>
 * Since : 1.0 <br>
 * Date : 2013-11-6<br>
 */
public class TransferObjectTask extends AbstractTransferTask{
	
	/**
	 * 执行迁移
	 */
	@Override
	protected void execute(IJndi srcJndi, IJndi targetJndi, boolean executeOnGenerate, String sqlFile) {
		List<ITransferModel> list = getTransferModelList();
		String action = executeOnGenerate ? "迁移数据库对象":"生成数据库对象DDL语句";
		CommonLogger.info("开始"+action+"，对象数："+list.size());
		long start = System.nanoTime();
		transferList(srcJndi, targetJndi, list, executeOnGenerate, sqlFile, action);
		long time = System.nanoTime()-start;
		CommonLogger.info(action+"完成，对象数："+list.size()+"，执行时间："+time/1000000000+" s,"+time/1000000+" ms,"+time+" ns");
	}
	
	/**
	 * 迁移一组对象
	 * @param srcJndi
	 * @param targetJndi
	 * @param list
	 * @param executeOnGenerate
	 * @param sqlFile
	 */
	private void transferList(IJndi srcJndi, IJndi targetJndi, List<ITransferModel> list, boolean executeOnGenerate, String sqlFile, String action) {
		if(null != list && !list.isEmpty()){
			boolean rebuild = true;
			for(int i=0,s=list.size(); i<s; i++){
				transferObject(srcJndi, targetJndi, list.get(i), rebuild, executeOnGenerate, sqlFile, i!=0, action, i, s);
			}
		}
	}
	
	/**
	 * 迁移单个对象
	 * @param srcJndi
	 * @param targetJndi
	 * @param model
	 * @param rebuild
	 * @param executeOnGenerate
	 * @param sqlFile
	 * @param append
	 */
	protected int transferObject(IJndi srcJndi, IJndi targetJndi, ITransferModel model, boolean rebuild, boolean executeOnGenerate, OutputStreamWriter writer) throws IOException{
		String name = model.getObjectName();
		if(!model.exists(srcJndi)){
			CommonLogger.error("源数据库中  "+name + " 不存在，忽略 "+name+"...");
			return -1;
		}
		if(!rebuild && model.exists(targetJndi)){
			CommonLogger.error("目标数据库中  "+name + " 已经存在，并且不需要重建，忽略 "+name+"...");
			return -1;
		}
		StringBuilder sb = new StringBuilder();
		if(rebuild){
			String drop = model.getDropSql(srcJndi);
			writer.write(drop);
			sb.append("\n");
			if(executeOnGenerate){
				executeSql(targetJndi, drop, model);
				//SpringUtil.execute(drop, targetJndi);
			}
		}
		String ddlSql = model.getDdlSql(srcJndi);
		sb.append(ddlSql).append("\n\n");
		writer.write(sb.toString());
		if(executeOnGenerate){
			executeSql(targetJndi, ddlSql, model);
			//SpringUtil.execute(ddlSql, targetJndi);
		}
		return -1;
	}
}
