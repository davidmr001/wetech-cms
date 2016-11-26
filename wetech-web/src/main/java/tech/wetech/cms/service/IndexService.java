package tech.wetech.cms.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tech.wetech.basic.model.Pager;
import tech.wetech.basic.model.SystemContext;
import tech.wetech.basic.util.FreemarkerUtil;
import tech.wetech.basic.util.PropertiesUtil;
import tech.wetech.cms.model.BaseInfo;
import tech.wetech.cms.model.Channel;
import tech.wetech.cms.model.ChannelType;
import tech.wetech.cms.model.IndexTopic;
import tech.wetech.cms.model.Topic;
import tech.wetech.cms.web.BaseInfoUtil;

@Service("indexService")
public class IndexService implements IIndexService {

	private String outPath;
	private FreemarkerUtil util;

	@Autowired(required = true)
	public IndexService(String ftlPath, String outPath) {
		super();
		if (util == null) {
			this.outPath = outPath;
			util = FreemarkerUtil.getInstance(ftlPath);
		}
	}

	@Inject
	private IChannelService channelService;
	@Inject
	private ITopicService topicService;
	@Inject
	private IIndexPicService indexPicService;
	@Inject
	private IKeywordService keyworkService;

	@Override
	public void generateBanner() {
		System.out.println("=============重新生成首页轮播图片====================");
		// 3、更新首页图片
		String outfile = SystemContext.getRealPath() + outPath + "/banner.jsp";
		BaseInfo bi = BaseInfoUtil.getInstacne().read();
		int picnum = bi.getIndexPicNumber();
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("pics", indexPicService.listIndexPicByNum(picnum));
		util.fprint(root, "/banner.ftl", outfile);
	}

	@Override
	public void generateTop() {
		System.out.println("=============重新生成了顶部信息====================");
		List<Channel> cs = channelService.listTopNavChannel();
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("navs", cs);
		root.put("baseInfo", BaseInfoUtil.getInstacne().read());
		String outfile = SystemContext.getRealPath() + outPath + "/top.jsp";
		util.fprint(root, "/top.ftl", outfile);
	}

	@Override
	public void generateBottom() {
		System.out.println("=============重新生成了底部信息====================");
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("baseInfo", BaseInfoUtil.getInstacne().read());
		String outfile = SystemContext.getRealPath() + outPath + "/bottom.jsp";
		util.fprint(root, "/bottom.ftl", outfile);
	}

	@Override
	public void generateBody() {
		System.out.println("=========重新生成首页的内容信息==============");

		// 1、获取所有的首页栏目
		List<Channel> cs = channelService.listAllIndexChannel(ChannelType.TOPIC_LIST);
		// 2、根据首页栏目创建相应的IndexTopic对象
		List<IndexTopic> channelTopics = new ArrayList<IndexTopic>();
		for (Channel c : cs) {
			int cid = c.getId();
			IndexTopic it = new IndexTopic();
			it.setCid(cid);
			it.setCname(c.getName());
			// TODO 显示8条，暂时固定写死。
			int num = 8;
			List<Topic> tops = topicService.listTopicByChannelAndNumber(cid, num);
			// System.out.println(cid+"--"+tops);
			it.setTopics(tops);
			channelTopics.add(it);
		}
		String outfile = SystemContext.getRealPath() + outPath + "/body.jsp";

		// 3、更新首页图片
		BaseInfo bi = BaseInfoUtil.getInstacne().read();
		int picnum = bi.getIndexPicNumber();

		// 从数据库取出最新的文章
		SystemContext.setPageOffset(1);
		SystemContext.setPageSize(5);
		Pager<Topic> pager = topicService.find(null, null, 1);
		SystemContext.removePageOffset();
		SystemContext.removePageSize();

		Map<String, Object> root = new HashMap<String, Object>();
		root.put("news", pager.getDatas());
		root.put("channelTopics", channelTopics);
		root.put("pics", indexPicService.listIndexPicByNum(picnum));
		root.put("keywords", keyworkService.getMaxTimesKeyword(24));
		randomKeywordClz(root, 24);
		util.fprint(root, "/body.ftl", outfile);
	}

	private void randomKeywordClz(Map<String, Object> root, int loopTimes) {
		List<String> keywordClzs = new ArrayList<String>();
		for (int i = 0; i < loopTimes; i++) {
			int radom = new Random().nextInt(6) + 1;
			switch (radom) {
			case 1:
				keywordClzs.add("am-badge");
				break;
			case 2:
				keywordClzs.add("am-badge-primary");
				break;
			case 3:
				keywordClzs.add("am-badge-secondary");
				break;
			case 4:
				keywordClzs.add("am-badge-success");
				break;
			case 5:
				keywordClzs.add("am-badge-warning");
				break;
			case 6:
				keywordClzs.add("am-badge-danger");
				break;
			default:
				keywordClzs.add("am-badge-primary");
			}
		}
		root.put("keywordClzs", keywordClzs);
	}

}
