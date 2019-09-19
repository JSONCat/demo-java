/**
 * 
 */
package com.sas.core.util;

import org.apache.commons.lang.math.RandomUtils;

import com.sas.core.constant.UserConstant;
import com.sas.core.meta.User;

/**
 * 马甲相关的函数
 * @author Administrator
 *
 */
public final class MajiaUtil {

	
	/***********
	 * 生成随机的马甲ID
	 * @return
	 */
	private static final long minMajiaUserId = 90000000000L;
	public static final long getRandomMajiaUserId(){
		final long id =  RandomUtils.nextInt(99999);
		return minMajiaUserId + id;
	}
	
	public static final boolean isMajiaUserId(final long userId){
		return userId > minMajiaUserId;
	}
	
	public static final boolean isMajiaUser(final String email){
		return email != null && email.contains("majia") && email.contains("@saihuitong.com");
	}
	
	/********************马甲相关的函数********************
	 * 所有马甲邮箱
	 * @return
	 */
	public static final String[] getAllMajiaEmails(){
		final String[] emails = new String[UserConstant.MaxMaJiaCount];
		for(int i=0; i<emails.length; i++){
			emails[i] = "majia" + i + "@saihuitong.com";
		}
		return emails;
	}
	
	/***********
	 * 根据商品或者活动id获取一批随机的
	 * @param goodId
	 * @param count
	 * @return
	 */
	public static final String[] getRandomMajiaEmails(long goodId, final int offset, final int count){
		if(count >= UserConstant.MaxMaJiaCount){
			return MajiaUtil.getAllMajiaEmails();
		}
		final String[] emails = new String[count];
		goodId += offset;
		for(int i=0; i<count; i++){
			emails[i] = "majia" + ((goodId++)%UserConstant.MaxMaJiaCount) + "@saihuitong.com";
		}
		return emails;
	}
	
	/************
	 * 是否马甲
	 * @param email
	 * @return
	 */
	public static final boolean isMajiaEmail(final String email){
		return email != null && email.startsWith("majia") && email.endsWith("@saihuitong.com");
	}
	
	/*************
	 * 创建一个全新的用户， id=0
	 * @param email
	 * @return
	 */
	public static final User newMajiaUser(final long sasId, final String email, final String avatarUrl)
	{
		int index = 0;
		for(final char ch : email.toCharArray()){
			if(ch >= '0' && ch <= '9'){
				index = index * 10 + (ch - '0');
			}
		}
		String name = null;
		if(index % 2 == 0){
			final String maleMajiaNames[] = ("支学民,石运凯,赏俊智,淡元洲,谷梁俊悟,腾彭越,严坚白,滕华清,昝斯年,马佳景彰,盍稷骞,王景辉,元高阳,暨君豪,彭曜瑞,暴起运,司徒智敏,甫嘉荣,宦皓君,文俊茂,凌英朗,寿俊远,敏高峻,伟泽宇,勇曜坤,鲜昊英,奈锐意,斛和昶,苌振翱,示英范,硕阳羽,弭弘益,植飞尘,求逸仙,闪文宣,姓俊捷,饶修真,士新立,萧辰锟,及永年,盈高轩,裔中震,姬越泽,势运凯,普晨涛,罕铭晨,柯腾骏,公叔运升,乌孙俊美,敖泰然,柴德业,渠海荣,西门浩歌,扶建修,用元甲,钭永昌,杜奇胜,鲜于长旭,蚁飞掣,蹉奇思,京翰藻,邵建木,登高明,锺星渊,靖嘉容,干雪风,留阳朔,郁宏达,阚成和,奕鹏鲲,冷明煦,苑昊硕,但祺福,市越彬,靳鸿煊,问敏学,皇阳波,池修德,希和泰,夙和悦,苏茂才,嘉光誉,仁曜栋,运宾鸿,溥锐利,孟高雅,哈志诚,辜修雅,庞鸿哲,谭光远,毋建业,钦昆皓,雪志文,萨范明,度乐咏,那文虹,宰父昌盛,米永春,何令雪,尤建白,第五弘量,翟英奕,李宏硕,钟离文山,万英哲,坚良畴,斐锐锋,貊承平,己圣杰,呼意致,霜高歌,镜皓轩,芒乐贤,板安怡,阴高原,镇和豫,帅建弼,侨辰沛,能才哲,樊长卿,牧昂然,赫弘盛,濮阳鸿宝,悉乐邦,守天和,明子昂,计刚捷,晁勇男,琦涵意,莘嘉许,虞欣荣,巨侠骞,柔鸿振,徭浩漫,冼温书,别修伟,莱和惬,隋经纶,白鸿达,桂鸿朗,么雅珺,瓮鑫鹏,赵和韵,刑俊豪,禚俊风,洪懿轩,少和煦,仆光临,闫兴德,隐翔飞,袁弘济,闵星海,禄阳飙,休茂德,拱承泽,云浩思,环星剑,零振平,咸鹏程,秦英悟,行永长,季海昌,承翔宇,谷旭尧,叔茂彦,戢晨朗,似蕴涵,蒙嘉祥,厍旭鹏,那拉彦君,羊舌德寿,檀嘉致,果宏爽,陀高芬,紫锐立,缪安民,籍景逸,肇鸿云,浮鸿风,阮景明,蹇鸿飞,充景同,戊鸿骞,将华池,五嘉树,哀晗昱,苗德润,旅国兴,仉阳飇,段君昊,戴文彬,岑光赫,祢明旭,波宏才,帛德馨,余海逸,狄良骏,宾承德,海嘉谊,昂建本,闳高超,宫鹏涛,屈骏伟,濮弘致,葛同和,伍弘新,鄂正谊,斋意远,孝正浩,介阳州,謇嘉良,贵志勇,龙锐翰,来和蔼,左辰龙,颜智渊,闽向荣,祈祺然,六昊焱,春浩然,詹鸿羲,商俊材,兰明德,汝和顺,钟鸿卓,岳新霁,福华藏,梁明志,奚飞羽,仍锐志,司寇维运,太叔凯康,壤驷泽民,检志业,书奇希,郝自怡,门昌茂,扈乐水,简德明,卷玉石,革承教,功鸿波,进波峻,荣鸿德,蓬星爵,郁正真,孔彬彬,拓跋运骏,郗嘉平,鹿玉堂,夏侯鸿志,苦玉宇,归安平,祖承基,毛玉轩,郸温文,端木嘉胜,谌飞扬,郎振博,望刚毅,禽宏远,督鸿祯,杭和悌,实俊良,绳宏儒,凭英耀,笃文柏,遇锦程,尧景胜,穰温瑜,线成礼,晏飞龙,崔兴发,建自珍,佟子实,潭德义,之高扬,绍康盛,旁瀚漠,仪智宇,仝浩波,许正志,栋嘉泽,邗乐音,桓玉山,年骏琛,柳伟晔,酒彭彭,车昌燎,野高韵,宝涵映,开康时,黄吉星,焦明喆,权阳焱,桥华采,覃文林,欧阳昆宇,温风华,修德容,琴烨磊,益嘉志,后涵容,侯运鸿,中英锐,颛孙文昂,奉鸿哲,繁子真,禹毅君,委子昂,堵运盛,经才俊,幸敬曦,纳弘义,德英睿,聊良工,张廖星鹏,贺骏祥,牟荣轩,祁令锋,汲昆纶,夫俊健,虎欣然,潜欣嘉,酆昆鹏,恽俊拔,操震博,乐正英才,揭咏歌,枝茂典,戏景天,本安晏,真建明,刚天材,子车睿德,殷阳夏,甘星泽,锐瑞渊,须奇致,费莫展鹏,宜俊人,完奇玮,易翰林,厉博雅,令狐元嘉,逢英勋,平凯定,隽修明,楼华容,勾兴生,诗力行,爱鸿志,泥高朗,舒力学,豆宏义,澄高远,展乐逸,崇弘阔,夷泽洋,富察宜春,伏玉书,慕容高洁,濯宇荫,锺离锐进,房安然,刘光熙,才安和,慎涵蓄,勤英毅,脱睿思,敬华彩,答文翰,犁乐正,掌明哲,漆雕英武,阳睿好,函鸿卓,巫振华,亓欣德,道高昂,念俊雄,抄鸿云,睢俊弼,洛安澜,倪昆杰,公西博艺,孙景福,唐鸿福,藤鸿禧,张修谨,荀昊然,齐飞鹏,母俊晖,乌雅景浩,包景铄,糜俊才,诸葛德运,安阳平,偶丰茂,公孙昂杰,轩辕乐天,薛鸿羽,麴力勤,兆弘毅,摩成周,矫向晨,曾立轩,魏峻熙,潮浩邈,禾浩初,沃俊达,时浩言,蒋俊语,万俟雅健,肥宏茂,寒安国,稽德泽,拜飞语,索智晖,蒲翰采,盘昆颉,仵睿才,庚鹏运,仲孙飞宇,僧苑博,成浩气,定弘雅,府俊艾,泣长运,力运锋,乾昊穹,章成化,善宏放,邢俊雅,眭文光,嬴成济,单冠玉,田温韦,虢振海,丹斌斌,戎昆明,业良策,绪兴贤,吴阳曜,捷鸿熙,律力言,管浩言,改永宁,裘晨潍,钞浩思,俟经艺,蔺博延,同宏盛,乌骞北,世俊喆,夹谷嘉瑞,羿彭勃,谯令璟,翁昊磊,库和光,劳弘大,澹台德本,乙元白,仰和玉,杨鸿畅,其阳泽,针子石,集令梓,聂飞航,危文乐,字绍辉,韶星阑,公冶正豪,树高格,东郭骏逸,燕奇文,钮和风,祭敏博,富嘉禧,隆伟彦,莫俊贤,嵇嘉澍,咎浩瀚,纵嘉纳,沈翰池,卫飞虎,召承运,曲弘亮,尉迟宏伯,叶浩淼,布经国,洋云天,宇文高澹,友成双,竭志行,姜咏德,合金鹏,高昊乾,朴康平,纪昊伟,於和颂,招修远,狂鸿福,谈成益,刁伟诚,飞正青,铎雅惠,庄华灿,磨安歌,金乐悦,庾飞舟,昌高杰,千伟毅,柏天工,邓宏深,徐嘉歆,烟立人,南门睿识,蒯涵煦,英兴国,卿康裕,熊兴昌,仙阳旭,申屠坚壁,苍浩大,冀建义,喻高达,台承宣,范姜昊东,频理全,鄞光辉,不兴运,言成龙,融文赋,况乐欣,匡兴邦,史飞跃,刀烨烨,释浩穰,清昆峰,表阳德,生开宇,冒鸿博,呼延永康,诺智志,闾安宁,是承颜,衅俊名,竺昊天,兴英逸,佼昂然,锁星洲,铁宣朗,衡佑运,闻人修诚,昔元纬,鞠鸿波,杞昂熙,迮锐阵,茂宜然,公德厚,亢俊发,赧鸿朗,辉勇捷,习雨石,回巍奕,关智杰,汤志尚,菅浩渺,旷朋义,薄英博,廖承允,红向明,彤昌翰,佘伟祺,印辰铭,娄意智,钱意蕴,歧明诚,延博涛,称振国,巧德曜,鲁经纬,位鹏鹍,声英卫,丘心思,校良哲,宏和硕,巩鹏涛,宛俊彦,伦礼骞,俎波光,华骏奇,谬博超,慈天华,东门鸿德,历乐山,学正业,漆昊宇,员昌黎,游承嗣,汉泰华,曹泰清,梅昊明,阎骞信,鱼浩大,边宜修,出骏桀,汗星文,桐智伟,毓兴学,辟嘉澍,丙项明,淳于高丽,于奇逸,卯涵煦,香嘉懿,赛昂雄,栾兴为,恭景天,类飞光,雀康泰,纳喇旭炎,蔚成弘,闾丘浦和,牵昆雄,欧明达,过嘉言,赫连康成,邰永昌,朋开朗,从修文,畅彭魄,初安易,良开济,董鸿达,墨良朋,胥雨伯,帖高岑,蓟建元,邬煜祺,鄢元龙,相运华,厚正奇,乘信鸥,姚高峯,军凯复,居乐人,吕曾琪,韦鸿光,桑翰音,班昌勋,郦英达,申康伯,郑职君,甄浩慨,宗政坚成,和康乐,候宜民,卑睿诚,容康适,弓凯乐,夕嘉珍,张简景曜,费昆卉,诸乐游,端骏哲,蛮高逸,井宏邈,怀蕴和,宇欣怿,百里弘博,麻鸿文,牛远骞,贡德惠,宿建茗,泉文德,楚运凡,城鸿煊,栗俊楚,皇甫景辉,褚振宇,练博厚,尉弘业,大阳辉,缑飞翮,上官阳荣,乔晗昱,舜鹏海,郜雪松,夔辰钊,皋承载,卢鹏海,亓官天罡,种信厚,常奇水,邶天翰,龚才捷,义元恺,路博文,江勇毅,秋德宇,佟佳星华,抗睿范,仲振强,邛睿聪,全承业,浦长平,袭嘉年,谢鸿晖,宓驰鸿,慕鸿畴,盛英喆,都良弼,查旭尧,闻良俊,霍飞文,胡浦泽,焉嘉实,敛良奥,尹志用,南哲圣,代俊楠,卜同济,漫元明,系玉龙,蒿雅畅,钊远航,花书君,御乐容,师凯捷,双景中,源和志,在宏畅,弘景行,圭玉泉,独国安,封良骏,晋锐逸,雍浩荡,藩恺乐,圣乐语,茆文景,玄玉泽,节俊力,老智鑫,丰翰海,贸文栋,鲍宏旷,扬瑾瑜,康德元,竹承恩,碧鲁骏喆,斯和宜,旗天纵,告斌蔚,伯承悦,任元驹,程星纬,森文石,九涵亮,丑昊空,浑华翰,光勇军,枚修永,邱嘉熙,骑成天,秘祺瑞,向翰学,堂炫明,乜阳曦,戚浩邈,丛德辉,寸修贤,犹振国,疏和通,礼茂勋,苟智刚,滑正文,方和畅,贝彬炳,茅元正,银永寿,说嘉运,芮涵忍,妫志义,殳鸿晖,韩玉树,宣英卓,养同甫,碧建树,随博裕,毕德庸,綦阳成,后光启,接光济,侍兴腾,辛锐精,席文耀,僪泰河,所博赡,愚子平,塞彦昌,藏向文,司光亮,单于运鹏,姒高驰,林运良,多兴平,驹宏峻,储鸿运,丁鸿振,顿华美,巫马金鑫,訾承安,羊冠宇,吾凯安,始星河,家鸿畅,荆嘉悦,瞿德水,窦俊德,邝朋兴,百英彦,星乐康,次德曜,梁丘鹏池,宰玉成,考永贞,裴新觉,羽立诚,水浩波,麦凯泽,原茂学,施经亘,宋良平,青明智,典德昌,佛瀚彭,连飞章,贲耘志,图门昆谊,尔志新,褒国源,郯思远,公羊雅懿,续玉宸,耿曦之,完颜承弼,应雨信,傅黎昕,束光明,罗泰平,司空项禹,山俊民,撒绍晖,瑞毅然,泰旭彬,首经武,由辰骏,谏弘文,戈文敏,朱良翰,逮天逸,赖文轩,东方彭祖,吉鸿祯,翠开霁,党兴思,璩澄邈,威正卿,税乐童,机波涛,隗坚秉,化海阳,茹明远,沐鹏云,蒉欣可,迟鸿信,伊运乾,汪振荣,壬涵畅,陶凯唱,长孙嘉佑,步安福,甲弘伟,析修竹,郏永思,载天路,永元思,泷睿广,郭嘉德,雷兴庆,剧文曜,巴德运,涂弘厚,蓝驰海,长彭泽,速思博,玉鹤骞,亥兴文,终鸿宝,粟子晋,越振锐,冉敏才,弥新曦,俞弘方,尾自强,前震博,淦广君,松涵润,悟泽语,宗华晖,艾浩初,务康顺,理高旻,资阳炎,太史浩歌,逯成业,闭华晖,仇季同,童文昌,衣弘和,黎乐章,利令飒,国晨濡,官建同,广振海,卓奇伟,冠咏思,素心远,蔡明知,佴俊能,廉绍元,农雅达,可锐泽,解致远,通飞星,巢哲瀚,潘乐生,牢兴修,左丘博耘,区思聪,陆飞英,邸升荣,穆轩昂,景安宜,喜昆鹏,武敏达,贯高旻,庆德华,励子默,错文瑞,买星宇,东耘豪,冯浩广,乐乐安,空兴朝,占元青,祝博实,微生安顺,寻智勇,天熠彤,阙鸿朗,营嘉茂,陈天成,符承天,强辰皓,奇高懿,翦奇正,范同化,尚宇达,惠高峰,睦嘉誉,第飞捷,性昌淼,折明杰,盖伟志,周星雨,屠哲彦,塔逸明,寇德明,允子琪,户乐湛,有宏逸,笪正平,象嘉颖,局凯歌,粘运浩,阿浦泽,却和璧,皮骞尧,信鹏天,风雅志,臧鹏鲸,无子安,邴力夫,肖英光,顾修然,章佳烨熠,古康健,夏鸿晖,段干孟君,贰嘉慕,凤天干,宁和正,公良文成,保修能,依俊誉,衷伟懋,庹阳云,马骏俊,战天宇,智光济,逄高义,荤伟茂,邹永望,箕志专,孛和裕,北鹏举,忻安翔,愈弘深,法凯旋,贾元勋,受哲瀚,司马寅骏,剑永怡,赤昊然,沙伟博,南宫康胜,卞琪睿,达高翰,湛良才,骆卓君,止海超,以兴安,满宇文,项腾骞,让阳舒,令新知,字睿诚,言奇玮,堂乐贤,官玉书,圣良骏,邰修竹,蓬兴怀,汉俊哲,张鹏煊,闽永福,五元良,宇振海,寇欣悦,淦嘉荣,六敬曦,褒鸿轩,无学民,戚海阳,纪博延,法文林,第五德泽,屈华翰,通建元,春鸿风,检德义,愚鹏赋,相承悦,仉和正,杭鹏海,谌天空,函元青,用宜修,竹成业,贾宏旷,邢同甫,东郭巍奕,简天工,受高峻,经元化,锁华茂,营彭魄,项志泽,皇子濯,彭飞鸾,薛浩慨,荣君昊,士天骄,苑安康,乐浩淼,尹熠彤,王旭鹏,郗斯年,占腾骞,端木伟泽,可巍然,柴天睿,万俟哲茂,初星晖,邱景天,裴鸿才,濯星爵,宗天赋,广承德,郦嘉德,旗文栋,毋元恺,果勇锐,谏瀚玥,张简昊然,佟佳高雅,同承望,袁经赋,阮高昂,环敏博,奉令羽,徐俊英,秦天逸,童俊雅,那承志,绳经亘,水博容,向昆雄,招华彩,闵泰鸿,潭景辉,容嘉祯,颜驰海,战高原,周子平,薄光华,琴明辉,隐宏爽,甄雅志,辛飞昂,贰德曜,零黎明,示俊智,段星津,糜立人,淳于维运,肥博耘,哈涵育,郸飞虎,邴康成,晁鹏鲸,偶英卓,印宏硕,闪高翰,福运恒,师高峰,展骞信,牵新曦,廖蕴藉,北浩瀚,功鸿远,聂文彦,匡弘亮,莘乐咏,威奇希,融绍钧,池雅达,冒明煦,乌昊嘉,阎康平,游昊苍,夔驰轩,寿运鹏,隆乐安,雍承福,于阳煦,骑宏峻,频康德,禽正真,邛睿识,商嘉澍,戊坚成,侍明杰,臧飞昂,宫昂杰,类景浩,符咏德,锺和怡,兰宏扬,赤金鑫,狄安国,富察祺然,希乐山,杜华灿,僪天泽,后宇航,宾承弼,崇修明,谷梁景行,郁安和,洋曦之,逮曜坤,顾俊雄,奇鸿畴,禄昊伟,佘寅骏,辜鸿德,林阳旭,卯昌勋,清嘉玉,谷良哲,倪浦泽,扈昊天,暴飞语,少永逸,冠振海,司鸿煊,穰高韵,魏伟兆,傅远航,卑乐语,戎宏恺,鞠炳君,度华荣,乘子石,掌博简,戈晨濡,邶奇胜,潘子安,泉正阳,斐天罡,帛星睿,乾运鸿,市运骏,锺离嘉佑,充经纶,理阳飇,权天翰,蓝子明,析星文,濮阳明德,杞天宇,尾昌黎,庄温书,习修德,武睿慈,乔雪松,枝澎湃,鲜和安,弘宏浚,满修远,敖斌蔚,亥飞章,仪浩广,竺逸明,焉浩歌,芒彦君,濮安易,壤驷泰和,琦良翰,红运升,金绍辉,庆弘盛,楼展鹏,休凯康,欧阳乐心,错学林,圭嘉泽,凤长岳,遇朋兴,钞昊焱,义和顺,么博易,蒯立轩,孙华晖,钮修洁,佴成化,时俊语,龚兴腾,鄂浩阔,太史茂才,危宾实,承星波,银博文,沃文康,苍俊迈,方宏富,岳弘厚,支嘉颖,牧良平,勾兴修,昌璞瑜,泣翔飞,天越彬,盈辰阳,历宾白,从光熙,熊志强,真嘉谊,郭星海,母英奕,覃永丰,召建柏,飞智伟,仲孙康时,象浩思,公孙修筠,以文乐,赵乐欣,晋奇正,杨俊发,随澄邈,史子琪,胥乐邦,华元凯,强旭尧,茅德泽,舒光济,轩辕光济,许成益,艾曜栋,乌雅兴德,党翰林,都昆峰,慎嘉致,雪飞尘,殷康宁,寸正信,邝德惠,化鸿哲,单于运莱,其咏思,夕涵蓄,庞长运,鹿元武,雷英博,逯震博,考良材,昔阳炎,门俊健,刁振国,修坚白,欧向晨,励宏逸,闫星光,令狐高轩,赧家骏,徭高芬,夙高懿,进俊彦,怀安顺,漆和玉,折飞跃,郏骏伟,年和昶,尚辰良,栗英逸,世成礼,卓鸿羽,谭鹏海,雀俊楠,卞飞飙,邵星火,茂建义,喻玉龙,仝昊宇,星辰锟,线哲圣,娄康复,穆兴言,洪奇志,苟鸿熙,城心远,宣安邦,大和同,却鹏飞,丙坚诚,惠鸿彩,乐正阳羽,吉同方,张廖承恩,植康盛,告志明,束宏深,计铭晨,郁敏学,律才俊,练恺歌,严永寿,蛮峻熙,闾浩涆,信弘雅,厚高超,呼英喆,公冶振博,佼晗昱,俟高旻,李飞鸿,庾浩旷,问泽语,鲍曾琪,鄢鸿骞,安德华,妫才哲,称文赋,蚁德佑,奈英纵,闳阳曜,台振翱,宓德辉,罗鹤轩,邸飞光,廉玉轩,赫鸿达,马成荫,忻星纬,巧安怡,渠凯旋,盘阳伯,蓟俊杰,才昆明,独昆鹏,达俊民,种锐翰,刘玉泽,钊修为,戢雅健,居博艺,乙海昌,靳恺乐,蹉浩漫,岑天和,中安宁,隽阳华,任乐游,闾丘同和,稽自明,呼延宏毅,蒲涵映,骆学名,松飞文,澹台星腾,裘乐康,依元亮,封瑾瑜,查阳飙,孛翰飞,易冠宇,左丘景曜,范姜高旻,浑彦昌,樊英资,司空绍晖,涂昊然,翟文瑞,缑志学,壬颜骏,尤博实,尉迟明远,梅中震,厍振强,叶阳州,腾鹏天,姜永言,绪明志,湛正志,森宏阔,智嘉熙,漫俊郎,缪俊才,学凯安,公叔欣怡,丑弘益,令良才,郜成周,刚光耀,绍令梓,宰父华辉,普弘和,性景中,载德业,家俊人,文昌盛,章成弘,盛锐逸,赏鸿信,箕飞捷,梁丘项明,冉元忠,虎德厚,操嘉容,来晨潍,蒙耘志,秘理群,紫星辰,光文德,章佳彬彬,鲜于宏盛,礼伟博,蒋高峯,登茂学,德良畴,让嘉歆,仍建同,羿宜年,豆俊能,荆兴文,夷哲瀚,萨长旭,说宣朗,韩伟毅,谈建业,越星剑,剧子民,留涵畅,燕庆生,山正祥,钭良骥,尔涵蓄,源鹏涛,似鸿德,滕浩初,郑宏胜,泥锐立,董宏义,韦骏俊,迟泽洋,伊学博,富思淼,霜兴国,洛英达,禹涵衍,贲宏放,羊舌温文,止刚洁,和修平,旁奇略,於鸿轩,在星华,翦和泽,硕阳晖,茹伟祺,捷昌茂,释曜瑞,善乐章,霍嘉福,仲志专,潮烨赫,贝立诚,常鹏鲲,慈宇寰,堵修诚,野浩荡,仵鸿云,碧鲁良工,及康震,隋鹏鹍,苦兴旺,喜天成,赛高寒,佟鸿卓,奚文山,寒翰音,长建茗,矫嘉赐,贡文华,东方才英,兴震轩,有奇思,逄嘉禧,闻人景辉,革伟才,谢伟志,翁凯唱,图门弘光,疏骞魁,白浩邈,关凯乐,开文斌,嘉鹤骞,孔昆谊,俎飞翮,祁运乾,凭国安,诺文柏,公弘文,枚弘济,竭嘉言,卿浩波,延翰翮,益永思,弭晋鹏,慕容雅珺,速俊贤,端才艺,巩翔宇,邬鹏池,乌孙新觉,冷彭薄,佛文成,刑乐悦,吕烨熠,荤温纶,嵇成双,康力勤,荀子昂,桓辰皓,菅俊晖,尧锐利,京伟彦,聊高畅,衅学真,朴俊材,萧鸿信,辉德运,勤圣杰,辟和裕,宿涵涤,机和光,烟英华,晏兴业,厉宜春,次天纵,季宜人,敏敏达,宏奇致,仆翰藻,督鸿光,纵骞北,庚坚秉,道英飙,太叔俊茂,集元明,陈正初,介烨磊,桥高阳,虞乐水,郝锐智,恽鸿振,笪星渊,顿骞尧,楚涵容,蒉意远,藩明亮,温咏志,朱令枫,陀志业,斯昆鹏,生昂然,夏英光,牛欣可,帅耘豪,锐乐家,虢骏奇,慕勇军,费俊驰,蹇瀚玥,酆烨烨,布力强,笃令璟,旷宏邈,潜昆纶,勇鸿雪,将斯伯,丛天瑞,赫连兴学,伦阳云,业驰逸,墨元龙,饶弘毅,子车高格,蔚才良,连兴思,汝昊昊,剑修然,仙安平,夹谷天元,本鸿振,老季同,范天路,公良高谊,耿国源,瞿泰初,香运凯,崔思聪,牟明达,铎嘉年,司马永年,宁鸿光,肇睿好,赖和韵,代承运,允瀚彭,西门高邈,愈鸿风,段干骞仕,陆宏朗,鲁俊风,舜宏大,陶浩大,姬向阳,井开诚,多阳焱,抗立果,拜嘉平,之伟茂,瓮鸿文,公羊凯歌,由文星,苗嘉慕,完颜飞星,行健柏,鱼刚豪,管飞翔,侨心水,滑辰骏,员思博,逢书君,终敏智,宗政建白,丹嘉祥,贵德本,祢锦程,袭景焕,昂智明,繁志文,风涵忍,索同化,宰阳嘉,泰正平,运波峻,栾浦和,汪璞玉,嬴经纬,申浩宕,首俊德,江智阳,贸睿广,訾祺祥,扶嘉悦,郎烨霖,汗承安,罕俊美,卷运良,表英武,仰宏儒,玉德润,军康伯,南宫光赫,貊兴平,卫乐成,储鸿羲,悟季萌,粟高飞,黄阳朔,席鸿博,弥烨华,司徒弘懿,百致远,弓蕴涵,汲康适,诗乐湛,尉乐童,翠开朗,睢嘉誉,盍经略,万君昊,甫宏达,曾涵涵,眭华奥,昝懿轩,凌浩博,丁子晋,焦学文,浮高朗,纳喇良策,位星然,卢和泰,那拉欣荣,俞元嘉,买成和,朋德昌,东门坚壁,泷雅懿,拱良骏,前晗日,郯翰墨,南门炫明,定骏琛,殳明旭,系开畅,藤浩皛,闻皓君,秋嘉石,衣博涉,念吉星,费莫飞羽,禾锐藻,马佳君博,肖鸿朗,巫元勋,程俊誉,谬茂勋,旅博赡,库浩言,续建安,树俊拔,能翰池,恭稷骞,微生涵润,帖锐思,桐浩壤,高安民,夫驰鸿,塞永嘉,包阳成,过俊晤,巢锐进,迮向文,局鸿祯,求博瀚,斛鸿祯,彤睿德,爱敏才,亓鸿志,黎承天,璩孟君,塔雨泽,姓和平,贺嘉志,撒鸿煊,海高义,石旭彬,抄建修,国翰学,澄锐泽,公西昂雄,余浩宕,毛瀚文,元昊然,钱高洁,戏嘉珍,邓永康,己辰铭,咎翰采,乜华采,畅英锐,步泰平,镇绍元,第驰皓,祖凯捷,祭伟懋,吴腾逸,摩康裕,犹皓轩,原兴为,云弘义,但昆皓,别苑杰,姚文昂,蔡欣德,祈宏才,柏越泽,蔺博裕,吾振国,委奇伟,坚建木,唐绍祺,力君昊,丰和璧,颛孙高逸,曲浩大,百里承基,回刚毅,车乐音,路明智,宋运诚,溥智渊,犁濮存,劳成龙,巴安然,皇甫鸿羲,永鸿远,窦学海,望高兴,隗俊远,须运锋,甘景彰,歧承颜,九曜文,桂兴发,敛玉韵,仇彭祖,龙才捷,伏元驹,保良俊,阙文耀,戴昊英,孟泰然,接和风,苏修文,宛苑博,斋文滨,羽同济,茆和雅,景昊硕,完英耀,贯浩气,麦运发,祝彭湃,毕承泽,莱永宁,税俊悟,诸振华,狂乐逸,碧海超,咸晟睿,田新翰,伍胤骞,御侠骞,衡俊艾,所睿明,漆雕光辉,班信瑞,平昆纬,籍文石,阳辰钊,羊烨然,板飞扬,房金鹏,毓志国,镜德运,双正诚,甲文曜,屠鸿才,不嘉澍,夏侯涵亮,褚乐正,幸浩波,南曦哲,阴烨烁,冼浩气,英文虹,况元德,冀浩歌,奕英彦,东鸿福,空弘致,芮彭越,宝德寿,脱志行,姒雅畅,韶锐阵,葛远骞,酒建章,府昊磊,睦兴昌,暨鹏涛,左海逸,淡宇荫,解新霁,良鸿晖,衷茂典,孝意智,候鸿飞,玄英豪,梁明知,单振荣,校云天,哀天佑,謇嘉树,友晗日,申屠烨伟,古安翔,瑞阳秋,牢正德,波玉宸,揭博雅,沐开济,务文彬,扬乐和,檀成文,皮景龙,柯英哲,禚英悟,邹煜祺,寻飞龙,靖晨朗,麴修能,户星泽,僧文宣,宜心思,利祺瑞,浦锐志,素景平,司寇明轩,叔鸿波,势浩然,书良奥,何鸿福,出弘化,亓官浩广,声浩慨,纳鸿畅,栋温瑜,是泰河,柳和煦").split(",");
			name = maleMajiaNames[(index / 2) % maleMajiaNames.length];
		}else{
			final String[] femaleMajiaNames = (
					"家芷蝶,公西念巧,司空依云,孟元绿,扬霞绮,洪清莹,郎若南,卫半安,敬芮佳,沈夏岚,翟傲儿,麻千易,次沛槐,晏铃语,公书艺,风静云,端木典雅,海春雪,程迎曼,鲜于雪枫,寇巧夏,赛晓凡,敖元冬,将弘懿,函奇文,麦悦来,摩芙蓉,宗晨曦,信安珊,骆云英,颛孙骏燕,拜可佳,宰凡灵,虢清奇,错泽恩,符璇玑,壬芳馥,赵碧玉,汉忆秋,道馨兰,富恬静,瓮问夏,理清昶,枝半青,国明煦,鲜晴曦,秘怜晴,夙寄蓝,敏安萱,邰慧月,门觅儿,用云蔚,功月杉,曹骊婧,告清晖,杞北辰,元初珍,赏兰娜,宿静枫,焦水冬,宫飞槐,法沛白,勇合乐,战碧白,鄂从珊,卢又琴,寿嫔然,泰逸丽,礼星晴,盖虹影,李怜翠,候迎梅,甄香巧,阙雨梅,台颖颖,邴颖然,王曼珠,疏靖儿,皮萧玉,青雅逸,何妍丽,仰思萌,费凝丹,牧野云,管莺韵,力蕙兰,蓝紫萱,森清一,定婉慧,让望雅,邝欢悦,夏浩岚,芮元柳,琦婉仪,计春冬,妫以彤,令白莲,艾飞荷,漆雕觅柔,瞿元蝶,谭春英,卯吉敏,允海桃,韶芮波,完颜秋灵,稽芃芃,郏笛韵,素斯乔,祭修敏,查暄和,应半芹,独紫丝,卞雪瑶,禚恨真,厚妙颜,隆醉巧,乔寒雁,经碧春,束秀梅,酒兰泽,丛湘君,宇文俨雅,房筠心,郸凝云,荆采文,阿莹白,竺暄莹,长水彤,繁觅荷,熊清漪,覃胤雅,公羊欣荣,贺梅英,公冶宛亦,夔水之,不雅韵,百凝远,尉迟沛春,宰父饮月,倪和暄,贰子美,申屠向秋,苟若菱,时菲菲,常飞兰,宝芮澜,蒿易绿,扈素昕,旗曼寒,烟如柏,谷以筠,磨凝琴,慕白亦,解英慧,夫雨灵,无傲薇,犁凝思,冉雅可,鲍昕月,亢夏菡,廉紫夏,种沛槐,单芝宇,俟从冬,陀安双,乐冰岚,湛芳泽,泥若雁,漆盼夏,楼梦玉,务怀梦,司马舒兰,旅依玉,潘甘泽,郝向秋,源清淑,阮寄蓉,潭岚风,太叔睿彤,丁绿蝶,袁玄素,多晶滢,韩问春,成友槐,昝语蕊,养傲松,苗冬易,朋丽珠,淳于梦露,杜从筠,励又槐,蹇笑卉,雍凝心,汤施然,索晓畅,休初柳,魏梓倩,荤心语,郯语心,牟惠心,澄慧美,冯傲菡,凌晓霜,谌友菱,毛锦文,仆雅媚,九灵安,浮乐安,箕蔚星,壤驷夏柳,车音华,徐冬萱,石书云,校一雯,悉夜雪,光凝珍,左悦怡,受涵菱,说初南,闪巧凡,靖觅双,畅婷秀,公叔淑慧,宓恨风,农笑柳,翠天蓉,锐绮琴,崇谷枫,易湛娟,扶易蓉,安华月,革素怀,阳忆彤,仪新梅,员红英,东晨星,邛听枫,丑忆柏,屠慧云,业叶春,殷碧玉,奕恬谧,习新雅,环玄静,乌问筠,泉乐芸,强思雅,但冬卉,羊舌吟怀,硕洋洋,步春荷,诗紫琼,武海蓝,留怜容,邱华婉,鲁慧月,欧以南,耿翠丝,芒傲易,招萍韵,出凝梦,董山蝶,百里寄松,剑迎夏,喻诗霜,威元英,迟斯雅,唐凡双,达映冬,尚幻梅,星靖之,冀蕴和,捷巧云,裘曼青,练彤云,綦银柳,郭清佳,考含玉,千妙双,花天薇,中诗怀,甲水冬,桥尔容,频含莲,永梦山,蔺曼蔓,弘喜悦,六婷美,善宜春,北清嘉,文雪柳,汝怀曼,五天蓉,随香雪,祖寄瑶,袭冰海,卜兰月,修曼容,广蝶梦,运雨筠,乜若云,都晏静,谬柔丽,蛮曼蔓,沙寒梅,操沛凝,濮阳天真,植绮晴,邶秀越,后芷容,柔梦桃,贯梦秋,钞惜梦,林代天,丘斯文,和旎旎,桑斯斯,平雅艳,单于巧香,庆柔谨,拓跋白竹,鄢芷若,纵新晴,盛水荷,铁熙怡,天霞姝,司半莲,贲天蓝,景玉英,库流逸,绪芬菲,伍梅青,英天真,刑智美,古凡霜,第凝绿,苍又香,蔚南霜,资灵槐,聊桐欣,蒙夏山,姚韫素,谢乐心,通夏柳,缪骊霞,居从露,竹秀曼,郁娅欣,子车馨逸,霍嘉惠,昌乐天,佟佳蔓蔓,行妙梦,曲以彤,郗倩美,德采南,甫觅双,官静安,蓟孤松,郁谷芹,关清怡,庄文静,介以寒,巧安梦,其颖馨,藤清逸,丹雁凡,盈妙菡,黄芷兰,问南琴,叶如容,盘友灵,乌孙丹红,镜雪绿,城初蓝,建宛筠,汲幼安,改羡丽,帛冰枫,亓官颖秀,系家欣,阴飞珍,权暄嫣,厍傲旋,桐芦雪,杭璇珠,慈清怡,腾忆枫,机心远,仉傲丝,区晶辉,涂娜兰,望双玉,南门飞双,塔静美,周美丽,邸文君,益元容,马佳傲菡,娄晓蕾,剧亦玉,万俟觅山,南宫南晴,奚帅红,謇寒荷,皋卓逸,郑雪曼,巫高洁,仲骊茹,勾依云,明楚洁,智雁菱,僧玲琅,节梦兰,完晓灵,象竹萱,公孙馨香,邵念天真,朱诗丹,栋颖初,东门从菡,辉嘉言,才傲玉,集安娜,穰睿思,哀雪珊,咸诗珊,莫依白,圭晴岚,容问萍,庹芳芳,樊悦欣,佼幼荷,迮玉环,局方方,咎晓蕾,声语丝,答姣丽,板冬莲,夹谷梓露,尾慈心,抄悦乐,师慕晴,哈向梦,辜书易,胥萍韵,史飞雪,彭湛蓝,萧洛妃,义映雪,齐红雪,穆语梦,却妙春,鹿丽泽,毕依美,华兰芝,吾如之,似寒凝,顿梓馨,凤罗绮,拱子菡,昂芬芬,念璇子,严冬莲,裔代容,良飞双,戈虹玉,弥香菱,茂天巧,席幻玉,督希慕,偶雨晨,南攸然,可飞莲,仲孙山彤,充叶丹,翁悠馨,嘉盼芙,祈春柔,孙玟玉,茅盼翠,友芳蕤,呼延姣姣,姬雨安,禾痴香,刁樱花,布访文,隐小星,尉清妙,赫海冬,雪玛丽,连冰安,潮采珊,危山雁,汗夜南,那拉思菱,池秀艾,弓欣笑,巴沛儿,鞠凝洁,葛雪羽,孛灿灿,寻傲冬,龙幼旋,巢向雪,泣凌春,乘长文,波绣梓,刚白薇,牵寻春,以运洁,字恬然,茹和悌,户忆彤,支南晴,费莫秋华,检芳林,段干芸芸,歧真一,栾含双,位晓瑶,劳松雪,度安柏,恭雅蕊,訾傲易,佛凌香,瑞静安,闻人宛白,龚翠桃,粘平安,施玉珂,濮和暖,邹涵柳,陶向松,须静娴,凭小晨,辛念桃,柯怀莲,历从蕾,诸水之,承幼怡,苌曼文,钟荷紫,阚心远,肥映天,亥荌荌,大优瑗,曾可儿,范姜淳美,伊羽彤,菅映菱,张晶灵,之叶农,母又槐,旁问玉,斋问寒,双琼怡,弭水风,东方尔阳,召雅旋,濯映菱,后岚翠,闫尔竹,相忻慕,衷嘉美,同晴画,帅丝萝,麴代珊,伟芳泽,第五晨辰,眭涵润,蹉熙华,飞虹影,势夏萱,轩辕绮怀,愈静雅,堂从丹,申兰泽,鱼宜嘉,张简春英,廖梦菡,空迎海,楚凝绿,代新美,呼冬萱,云怀薇,鄞绮露,仙隽洁,伦雪容,彤和暖,爱清昶,朴姮娥,融半凡,刘芷文,商忻畅,乙夏旋,么安筠,蒯霞赩,荀灵凡,封忆秋,巩寻梅,丰又蓝,折怀寒,典冷雁,包舒荣,颜叶飞,邓乐悦,墨典丽,斛思卉,豆思柔,钦若芳,赧佳思,忻仪芳,梁丘访天,皇清涵,闵寄琴,有贞韵,白沈靖,戊丹彤,逢逸云,邬映天,浦晓莉,初尔烟,游夏青,逄尔蝶,蒉奥维,欧阳亦丝,那易文,金新语,亓小霜,表长逸,佴慧晨,段雨琴,粟寻桃,贡灵慧,窦碧琳,滑白梅,宁冰莹,闭颖馨,万小翠,季青雪,乌雅歌云,宜智美,恽冰心,所天蓝,吉静云,归恨荷,微生倩秀,荣痴春,淦香之,斐娜娜,燕幼怡,孔娅童,玄梓楠,合雅宁,祢萦怀,宛幻梅,笃飞兰,始雅静,巫马惜筠,隗雅韶,谏念文,黎又松,崔珊珊,原绿凝,东郭婉丽,冷莹华,柏雪晴,伏睿姿,牢紫南,喜霞飞,奇怀柔,堵春梅,士睿哲,俞孤容,展飞绿,禽凌春,福青寒,寒迎彤,冒幻竹,干畅然,檀冬卉,慎芸馨,谈润丽,依寄蕾,陈长菁,田品韵,尧贝晨,长孙慧秀,抗丹云,是迎丝,章映阳,勤鸣晨,糜天音,税雅寒,奈从安,接碧萱,莱舒扬,真听荷,温丹亦,井沈静,实淳静,舜听露,仇凝冬,香瑞云,锁希恩,蒲笑雯,宋语蝶,晋玟丽,厉依波,伯莹莹,柳盼兰,载兰若,暨浩丽,茆妙珍,薄春海,苑雪曼,闻清润,过千易,诸葛兰芝,前梦凡,满雅云,淡嘉丽,江怜烟,能文敏,乾尔风,吕怀绿,宦云臻,左丘忆南,于恨竹,遇听安,生沛容,己幻儿,富察涵阳,冼致萱,聂凌春,少丹翠,虞怜雪,圣从筠,丙思琳,僪迎波,营从蓉,祁隽巧,矫瑞彩,罗盼易,盍雪兰,塞妍妍,昔浩岚,赫连如南,京绮彤,宣听南,奉映雁,雷依然,柴云蔚,屈驰丽,睢凌柏,桂诗蕊,刀翠霜,纳喇如风,缑悦和,禹珺娅,梁梦安,谷梁一瑾,禄暄和,巨和怡,宏正清,枚婉秀,秋水蓉,旷秀艳,紫沛雯,虎新柔,碧鲁新柔,庚碧春,苦忆梅,析怀蕾,山惠美,赤语林,胡冬菱,衣晗玥,蚁访冬,夏侯天玉,首忻忻,饶冰珍,纪丹红,尤香天,嵇春蕾,骑竹筱,线隽美,敛芫华,殳聪慧,宗政暮芸,速白易,果春岚,於梦寒,进夏彤,续清懿,揭灵萱,贵梅梅,况乐荷,贾悦欣,惠冉冉,水语蓉,买凝丝,守晴丽,卿梦菲,戏白枫,兆巧蕊,薛思敏,孝晓畅,御芷波,向梓柔,郦秀越,贸如曼,陆雯君,针谷兰,项依瑶,简珉瑶,浑诗文,卓柔静,康悦畅,戢彦珺,侯夏寒,卷宛秋,籍静柏,甘凝莲,臧梦玉,回晶霞,俎宛凝,庾语梦,撒寻梅,及飞雪,邗访波,司寇嘉颖,止蕴秀,犹茗雪,岳惜寒,顾妞妞,碧爰爰,逯雅美,詹湛恩,雀令婧,言梦旋,侨语柔,卑诗蕊,徭琰琬,普半蕾,绳巧风,清又莲,藏盼夏,韦半双,闾丘亦绿,端芸姝,兴忆丹,府惜玉,藩沛凝,司徒绮艳,叔幼霜,银小星,靳凌雪,羽丽芳,蔡萱彤,延雅丽,酆香萱,宇华乐,尔秀媛,树唱月,愚阳霁,世以丹,栗淑懿,令狐冰冰,隋幻巧,边问蕊,衡涵山,宾若骞,肖听筠,利初柳,闽哲美,洋平安,佟采莲,年晶灵,岑晗蕊,寸麦冬,仝芳华,皇甫安安,褚迎南,帖曼寒,班梅花,琴恨瑶,脱香巧,零傲冬,图门清芬,暴若华,希芳洲,西门暖姝,苏长霞,野尔蓉,姓雨彤,阎佩珍,戚迎天,在觅云,余沛蓝,储夏兰,公良香春,军碧曼,诺觅丹,马盼香,印思宸,太史孤容,衅悠雅,化芮欢,由书兰,委海儿,竭慧雅,范凌春,钮晗玥,驹幻翠,开丽姝,夷妙思,占和悌,称韵诗,闳莹琇,全春桃,羿晓瑶,姜萌阳,慕容怀玉,焉淳雅,貊幻香,裴奕奕,钭端雅,本巧夏,任乐巧,贝向真,肇慧丽,狂晓丝,仍玉兰,蒋安容,匡卓然,掌新冬,渠清绮,滕语柳,舒清华,类新之,晁采白,律含雁,汪雁桃,章佳以蕊,牛幻珊,毋童彤,戴凝然,璩嘉怡,党寄真,闾怜烟,学丹彤,释海桃,路阳阳,戎南霜,红忻乐,锺离添智,梅寄容,童宵雨,谯又绿,钊雪曼,上官听枫,仵痴凝,米尔芙,许曼卉,锺夜蓉,莘若南,佘白柏,蓬尔真,罕湛雨,求安柏,毓家美,坚寄灵,幸碧白,玉青梦,越思迪,吴寻巧,尹初珍,嬴珠佩,乐正婀娜,性若云,桓以莲,绍悠柔,祝君丽,方诗蕾,狄秀竹,怀雨筠,笪惜香,悟又青,潜怜晴,钟离绿海,辟新荣,逮依琴,姒如冬,春嘉音,张廖书萱,市从凝,泷丹秋,钱和美,斯皓月,隽凝雪,赖诗双,沐痴海,翦紫杉,郜谷秋,侍若云,兰寄南,冠雪冰,终寒梦,高宛亦,秦宛秋,仁白筠,沃绿凝,澹台书语,睦代芙,傅湛颖,褒燕晨,保又绿,示思松,庞嫚儿,别翠茵,萨彩妍,洛新梅,老新烟,镇春柔,羊寻桃,来巧香,邢丝祺,漫念露,霜慕卉,纳慧丽,溥天骄,杨恬美,松凌霜,书雍雅,登琨瑜,从怀思,铎冰蝶,夕彦红,刑若薇,暴念柏,泣春兰,乔幼珊,苌凌蝶,府淑兰,苍友易,钞竹萱,岑顺慈,萧碧春,学梧桐,濯湉湉,杜惜寒,折冬灵,建梓彤,姚向山,白古韵,宏秀美,愚芮静,马佳笑槐,沈秋灵,东方笑容,汗含文,贺依珊,历翠丝,务碧玉,晏玄静,索野雪,帖新竹,望梦竹,谷芳菲,迮西华,唐梦菲,犁傲安,希凌晴,后冰岚,毋清晖,频巧香,赏芳菲,泥莹然,骆智美,申屠骊英,庆霞赩,姓幻丝,台初晴,花慧月,贰醉香,运香天,雷雨竹,谢从蓉,佟半芹,寸冬莲,孛朝旭,清诗双,司马之双,田安波,素芷荷,邓问香,贡凝雁,姜盼香,达清妙,肇问萍,甄孤晴,督暮芸,敏锦曦,其和怡,冉瑞灵,暨香菱,局晓燕,赛宜嘉,罕悠逸,时素怀,么思美,俎春竹,尤曼岚,集悠婉,星凌雪,山碧白,剑素欣,闻人雅隽,缪雪羽,宾若南,蚁湛芳,令灵萱,常梦菲,戢青烟,祢笛韵,辉含玉,母青枫,茆尔冬,邹平卉,马颖初,似安柏,性晗蕾,示云溪,蔡雅晗,香欣彤,练寒凝,公叔寄蕾,聂梦山,严清漪,甫以寒,邴馨逸,旗若南,之易蓉,钦飞兰,吾嘉歆,泉春梅,掌丹云,淳于春芳,贲山菡,类沛文,何瑰玮,惠芮雅,元水丹,迟雁桃,琦依瑶,郁葛菲,雀森丽,蓝幼霜,于平凡,用丹秋,章清妍,菅孤兰,富怡畅,森谷槐,盛洁静,剧妞妞,殳秋颖,禹书文,友小翠,晋清韵,司空之桃,速诗蕊,沐静美,候逸丽,竺伶俐,舒清霁,韦冰洁,秘曼冬,单于英华,琴月华,箕淑哲,巨紫雪,穆白梅,麦玟玉,力晨旭,念梓敏,牟斯文,浑南蕾,太史孟阳,苏晶灵,偶小珍,殷秀曼,齐萍韵,首晨曦,乌雅白风,道云韶,嬴颖慧,利采梦,皇尔白,印书萱,竭秋珊,滑从霜,濮阳小凝,六翠芙,冒洁玉,郝燕舞,功秋寒,红思嘉,管令慧,骑向松,酆芳蔼,福虹彩,商红旭,端木静婉,寇若骞,蹉雅柔,朋寒凝,路冷萱,丙霞影,禽诗柳,宿艳娇,向灵雨,谬新梅,任忆南,颜灵卉,经幼珊,濮乐蓉,詹妍芳,真桃雨,华婉然,延芳苓,兆弘丽,律若兰,招愉心,嘉曼蔓,衅尔容,锁碧巧,师晓蕾,褚觅柔,钟离真一,卷醉柳,函静枫,中文漪,戊丝琦,莘朗然,宰芮悦,南安静,岳思懿,闳雁卉,厚幼白,糜梦之,有惜梦,绪凌春,始语海,己书桃,藩诗晗,东郭柔妙,衷幻玉,邛寒云,宛向薇,荣暖姝,展醉柳,龙青枫,宋元芹,铎雅爱,冯雅逸,柔春冬,松春英,将莺语,匡夏柳,蒯白易,虞凡霜,果访梦,绍雪容,强丹秋,宰父湛恩,庹冰香,梁仙媛,计晓凡,云绿夏,怀雨筠,纪月明,允宜欣,甲紫丝,曾真茹,满雪晴,咸梦竹,赤飞烟,阎静云,赖如柏,宣采萱,区冬萱,大会雯,劳从雪,钮向露,鹿玲琅,郭寄南,方依丝,尔琨瑜,夹谷春柔,辛凡桃,奕珠星,仰婉娜,碧鲁初瑶,季半青,由香薇,闾沛凝,春荷珠,在傲旋,巫蓉蓉,蒋雁卉,秦惜芹,睢问柳,明古兰,军野云,胡陶宁,公羊尔蓉,傅仙仪,应曲文,烟静槐,储湛雨,昂玉琲,司寇芸芸,弓彩萱,初曼青,莫尔竹,邢安吉,千文惠,孔念云,召雅惠,磨元容,游熙柔,诸葛雨南,勾依秋,屈初阳,许初蝶,黎贝莉,杭碧菡,泰天睿,线叶欣,鞠春华,鲁佳思,百华楚,侯春燕,赵恨瑶,巴芳洲,狄又菡,字芳泽,藏语燕,墨庄丽,裘骊萍,戴天真,敬水丹,操谷雪,洋子怀,公良天曼,系舒方,芮修洁,旁初雪,漆丹山,纵思义,夙施诗,郦飞莲,尚柳思,进含巧,保娟秀,板冰蓝,越莹华,员兰娜,图门家馨,宁昕葳,能令怡,萨翠巧,蒿怀蕾,羽芷天,曲夏兰,随琇莹,俞孟乐,咎若灵,五欣愉,恭沛柔,贯子怡,隋又蓝,庚碧蓉,驹语蓉,仪怀慕,枚念梦,步令梅,卯丹红,葛天蓉,占绿夏,拱悦远,厉浩岚,锐家美,广元灵,海雪枫,业绮兰,纳慧捷,佴斯雅,帛丽雅,塔含巧,兰春娇,以晨曦,洛怀芹,貊曼丽,景婉君,及代丝,答芳蕤,融凌丝,呼延婉秀,亓官向真,智嫔然,树雅歌,肖傲南,官以晴,犹长文,宝如凡,万香桃,赫连丽芳,从心语,张凡儿,赧书白,斯琛丽,单涵润,城思菱,栋初蝶,笃梦凡,水盼兰,江紫杉,铁卓然,闵浩丽,冠林帆,祁寄文,伏紫南,燕雪容,实冬卉,康唱月,银初阳,汤秀慧,戈如冰,滕娟娟,昌问雁,陀正清,焉婷秀,载凝竹,欧阳晨钰,英清秋,查丹蝶,桥恨真,公冶访天,拓跋涵蕾,抗湛蓝,改碧蓉,鲜飞雪,朴曼彤,苑和静,宇文妙婧,封璎玑,市典雅,资长菁,邝傲云,充安筠,沙雯丽,敖丹寒,裔文静,郯若菱,那雨筠,梁丘雅丽,朱迎秋,威心香,承尔槐,德清佳,耿寄春,哀晓凡,解子蕙,盍晨旭,司霞月,兴舒怀,寻琼芳,郸和暖,成晓骞,梅天曼,是念之,坚从筠,丹元彤,勤雅韵,童仙韵,波清韵,义友卉,芒谷兰,逯丹寒,阙馨香,谌兰芝,隆惜文,辜浩岚,罗忆远,翠婉容,吉元芹,金银柳,栾惜玉,巩紫雪,堵凡雁,荀欣畅,余诗霜,逮平春,闽平蓝,习天恩,班傲雪,支闲静,衡紫文,范莹琇,漆雕晓瑶,奚初珍,郑霞飞,风蓉蓉,上官睿姿,牧新儿,善白莲,雍初阳,姬碧蓉,纳喇灵松,伦安寒,夏念瑶,毕忆彤,伟香馨,羊舌香卉,苗文茵,伯昭懿,开智敏,释冰洁,蔚喜儿,左心远,张廖幼丝,乜初彤,虢清芬,施凌波,杨浓绮,肥童欣,戎凝云,声新雪,锺离梦竹,少曼彤,章佳逸云,牢诗桃,仙以莲,闭春柔,夏侯清芬,旅凝蝶,环宛菡,沃华乐,伍觅双,前忻忻,泷雅致,表秀艳,弘芝英,闪梓馨,吕凡巧,李韶美,蒙琦珍,逄恬谧,植沈静,瓮霞文,邗君丽,弥凝思,佼婉静,荤和平,祖子舒,刁沛岚,权瑜璟,樊宛秋,叶若山,乌惜雪,厍逸馨,庞歌云,夷春琳,双佩珍,化平彤,休尔竹,镜梓菱,无清逸,冀亦巧,闻逸云,竹小晨,行尔烟,第访彤,扶梓琬,才芷蝶,符南琴,昔湛英,平骊蓉,子车愉婉,腾秀妮,苟修敏,斋惜玉,来伶伶,鱼寄灵,藤元绿,邵依波,荆茹云,扈瑞绣,曹雪巧,普之桃,眭靖儿,贵绮怀,胥清怡,士米雪,盘从阳,项雪瑶,连寻绿,舜凡白,仍琳芳,崔寻桃,鄞春海,阿丽姿,乘家美,桑芳馥,巢秀洁,独诗兰,留夏月,危映冬,窦安容,接芳泽,隗迎南,九帅红,斐盈秀,居清逸,南宫夏山,安梓舒,乙春芳,晁青香,阳寒梅,圭璇珠,瞿纳兰,桐冰巧,容冰真,依痴凝,受佳文,谯寄松,绳芸芸,锺和玉,老望慕,柳雨莲,爱晓霜,司徒亦玉,车茵茵,象语燕,礼秀筠,宇海女,须月明,柴艳卉,镇明熙,刘苇然,檀安荷,孙云蔚,节璠瑜,彤今瑶,张简佁然,裴惜海,邬智宸,申雅蕊,战冷雪,良平绿,公西丹蝶,野素华,谷梁白柏,禄慕凝,法美华,农乐欣,玉乐安,全盼巧,鲜于欣艳,乐正安娜,秋寒天,养暄妍,守雨雪,书宛秋,段干慕晴,析怡月,郁小萍,原冬菱,古蔓菁,却含云,励欣悦,左丘令婧,敛璇珠,湛尔云,逢静涵,洪依童,井依云,揭觅晴,包雪绿,错听安,过依秋,倪文静,饶畅畅,豆清懿,夫惜玉,聊若英,悉飞阳,合乐英,库晴岚,乾忆然,本谷芹,牵诗蕊,鄢小霜,欧素洁,登莎莎,皇甫新月,夕奇文,相童彤,僧梓榆,长怿悦,廖梦旋,摩萱彤,邱雯丽,圣飞燕,终觅柔,翁流丽,杞雨双,源晴霞,年香梅,隽涵涵,盖昕靓,黄格格,奇凌寒,狂寄文,庾岚翠,楼雨文,臧乐松,遇珍瑞,王书蝶,脱靖之,太叔安卉,通音景,辟悦心,皋安娴,公孙青文,巫马沛蓝,凌兰泽,东门隽洁,长孙清舒,謇嘉宝,飞明轩,笪清晖,鄂代珊,祈芷云,陶灵韵,赫叶春,微生恨寒,董春岚,仵虹颖,袁曼吟,羿优扬,仲孙芃芃,旷婉丽,澹台忻欢,蛮寒香,次半香,艾飞双,毛以彤,丑畅然,史书竹,阚靖易,雪小蕾,玄红叶,颛孙元旋,校怀蕾,慕容如风,仇安莲,度菊华,钱欣怡,温珠佩,郗忆梅,谭海白,澄沈靖,仝琼华,党忆文,蔺春桃,高曼寒,尉寄松,汪妙之,盈丹翠,东凌翠,孟访冬,侍夏菡,尧以珊,彭凝荷,陆红艳,检欣跃,仉静姝,孝芳春,侨芳润,钟沛容,凭千凡,北语梦,牛夏璇,焦念文,籍元容,廉逸美,卫翠阳,零岚岚,池寄真,霜慧颖,宫晓旋,永子琳,干向卉,喜骊燕,费凝静,楚熙星,信山芙,薛梅英,禾白梦,魏叶丰,邶冰冰,和清卓,青运洁,丁云水,勇芳润,可旎旎,宗绮琴,蒉琬琰,归婉慧,石芮丽,宜晓楠,那拉淑婉,续迎梅,亥以柳,告南露,綦醉巧,哈清华,出平晓,吴觅翠,畅红豆,毓半双,乐悦畅,西门白玉,完颜醉蝶,贝听荷,忻碧螺,世迎荷,亓尔蓝,缑嘉懿,端乐悦,房绮露,别傲玉,郜流如,万俟丰熙,鲍寄灵,崇香天,粟凝绿,浦书雁,况馨荣,亢思萱,阴海莹,韶怜翠,称觅双,乌孙念巧,郏谷之,斛冰双,种南晴,革吉玟,桓云臻,衣沛雯,麻夏兰,伊海桃,后叶帆,完妙梦,家怀柔,理牧歌,慎怜容,蓬丽芳,瑞曼辞,渠蕴秀,塞密如,祝元冬,针宜春,汉幼柏,丰云岚,介心香,武雅柏,潮安安,硕亦玉,闾丘歌韵,柯清卓,翟玮奇,徭香柳,奈云淡,钊元槐,淡香馨,阮春梅,光夜香,易丽泽,矫丰雅,隐绮梅,尾芬馥,第五雅霜,考萍雅,翦雨琴,悟书白,庄婉娜,壬晓兰,公迎曼,卑冰海,莱宵雨,富察香薇,简叶农,席晓灵,淦秋玉,禚秋蝶,束如波,机琴轩,撒合乐,但溪儿,奉惜文,薄清昶,谏筠竹,熊婉秀,诸忆梅,郎安萱,茹醉巧,修清涵,典骊红,冷香芹,幸晓蕾,说致欣,钭问玉,刚新雪,卿小春,麴睿文,卢晶瑶,周沈雅,徐寄瑶,慕丽珠,拜安安,潜敏叡,漫绮玉,宦冰菱,巧听安,捷忆秋,溥觅山,紫可心,尹映天,丛巧夏,茅傲柔,所曼云,昝秋荣,寒初晴,费莫天音,诗骊茹,袭雅寒,璩依白,酒春晖,娄晓君,言凝洁,空冷梅,丘书语,多玄素,顿巧香,穰怀芹,睦怜南,止骊颖,汲又琴,戚湛芳,寿依琴,仁霞雰,贾从珊,僪翠桃,代绮琴,户冬梅,柏慧雅,国慧智,都凌蝶,卓涵易,边傲易,褒诗翠,不谷蕊,令狐星瑶,邸谷云,税碧琴,尉迟孤菱,碧多思,靳思云,茂丽容,定傲易,谈芬菲,繁书文,顾英楠,桂淑贞,同可可,门宛亦,仲曼蔓,於怀柔,潘晓燕,稽秋灵,百里献玉,势星菱,卜雪卉,疏秋白,弭向雁").split(",");
			name = femaleMajiaNames[((index+1) / 2) % femaleMajiaNames.length];
		}
		final User u = User.newUserByEmail(email, avatarUrl, MD5SignUtil.generateRandomKey("1234567890qazwsxedcrfvtgbyhnujmikolp", 8), System.currentTimeMillis());
		u.setNickname(name);
		u.setSourceSasId(sasId);
		return u;
	}
}
