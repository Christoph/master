package server;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.corundumstudio.socketio.SocketIOClient;

import core.json.gridHistory;
import processing.Composite;
import processing.Helper;
import processing.Postprocess;
import processing.Preprocess;
import processing.Spellcorrect;
import processing.Weighting;
import core.Tag;
import core.json.gridOverview;

public class Workflow {
	
	// Initialize variables and classes
	private Helper help = new Helper();
	private Weighting weighting = new Weighting();

	private int globalID = 0;
	private int count, packages;
	private int lastAppliedStep = 0;

	private Boolean running = false;
	private String mode = "";
	private Boolean dataLoaded = false;

	private Boolean preDirty = false;
	private Boolean spellDirty = false;
	private Boolean compDirty = false;

	private String stopwords = "a,b,c,d,e,f,g,a,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,0,1,2,3,4,5,6,7,8,9,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
	private String english = "a,abandon,ability,able,abortion,about,above,abroad,absence,absolute,absolutely,abstract,abuse,academic,accept,acceptable,access,accident,accommodation,accompany,accomplish,accord,account,accurate,accuse,achieve,achievement,acknowledge,acquire,acquisition,across,act,action,active,activity,actor,actual,actually,ad,adapt,add,addition,additional,address,adequate,adjust,adjustment,administration,admire,admit,adopt,adult,advance,advantage,adventure,advertise,advertisement,advice,advise,adviser,advocate,affair,affect,afford,afraid,after,afternoon,again,against,age,agency,agenda,agent,aggressive,ago,agree,agreement,agricultural,ahead,aid,aim,air,aircraft,airline,alarm,album,alcohol,alive,all,allege,allow,ally,almost,alone,along,alongside,already,alright,also,alter,alternative,although,altogether,always,amaze,amendment,among,amount,analysis,analyst,analyze,ancient,and,anger,angle,angry,animal,announce,announcement,annual,another,answer,anticipate,anxiety,anxious,any,anybody,anymore,anyone,anything,anyway,anywhere,apart,apartment,apologize,apparent,apparently,appeal,appear,appearance,application,apply,appoint,appointment,appreciate,approach,appropriate,approval,approve,approximately,architecture,area,argue,argument,arise,arm,army,around,arrange,arrangement,arrest,arrival,arrive,art,article,artist,as,ashamed,aside,ask,assess,assessment,asset,assign,assist,assistance,assistant,associate,association,assume,assumption,assure,at,athlete,atmosphere,attach,attachment,attack,attempt,attend,attendance,attention,attitude,attract,attraction,attractive,attribute,audience,aunt,author,automatically,autumn,available,average,avoid,award,aware,awareness,away,awful,baby,back,background,bad,badly,bag,balance,ball,ban,band,bank,bar,barely,barrier,base,basic,basically,basis,bath,battle,be,beach,bear,beat,beautiful,beauty,because,become,bed,bedroom,beer,before,begin,behave,behavior,behind,belief,believe,bell,belong,below,belt,bend,beneath,benefit,beside,besides,bet,between,beyond,bias,bid,big,bike,bill,billion,bin,bind,biological,bird,birth,bit,bite,black,blame,bless,blind,block,blood,bloody,blow,blue,board,boat,body,bomb,bond,bone,book,boom,boost,boot,border,bore,borrow,boss,both,bother,bottle,bottom,boundary,bowl,box,boy,brain,branch,brand,bread,break,breakfast,breast,breath,breathe,breed,bridge,brief,briefly,bright,brilliant,bring,broad,broadcast,brother,brown,brush,budget,build,bunch,burden,burn,burst,bury,bus,business,busy,but,button,buy,buyer,by,cable,cake,calculate,call,calm,camera,camp,campaign,can,cancel,cancer,cap,capability,capable,capacity,capital,capture,car,carbon,card,care,career,careful,carefully,carpet,carry,case,cash,cast,castle,cat,catalog,catch,category,cause,celebrate,celebration,cell,cent,center,central,century,ceremony,certain,certainly,chain,chair,chairman,challenge,chamber,champion,championship,chance,change,channel,chapter,character,characteristic,characterize,charge,charity,charm,chart,chase,chat,cheap,check,cheek,cheese,chemical,chest,chicken,chief,child,childhood,chip,chocolate,choice,choose,church,cigarette,circle,circumstance,cite,citizen,city,civil,civilian,claim,class,classic,classical,clause,clean,clear,clearly,climate,climb,clinical,clock,close,closely,clothes,clothing,cloud,club,cluster,coach,coal,coast,coat,code,coffee,coin,cold,collapse,colleague,collect,collection,college,color,column,combination,combine,come,comedy,comfort,comfortable,command,comment,commercial,commission,commit,commitment,committee,common,communicate,communication,community,company,compare,comparison,compensation,compete,competition,competitive,competitor,complain,complaint,complete,completely,complex,complexity,complicate,component,compose,composition,compound,comprehensive,comprise,compromise,compute,computer,concentrate,concentration,concept,concern,concert,conclude,conclusion,concrete,condition,conduct,confidence,confident,confirm,conflict,confuse,confusion,connect,connection,consequence,consequently,conservative,consider,considerable,consideration,consist,consistent,constant,constantly,constitute,constraint,construct,construction,consult,consultant,consume,consumer,contact,contain,contemporary,content,contest,context,continue,continuous,contract,contrast,contribute,contribution,control,controversial,convention,conventional,conversation,convert,convince,cook,cool,cooperation,cope,copy,core,corner,corporate,corporation,correct,correspond,cost,cough,could,council,counsel,count,counter,country,county,couple,course,court,cousin,cover,coverage,cow,crack,craft,crash,crazy,cream,create,creation,creative,creature,credit,crew,crime,criminal,crisis,criterion,critic,critical,criticism,criticize,crop,cross,crowd,crucial,cry,cultural,culture,cup,curious,currency,current,currently,curtain,curve,custom,customer,cut,cycle,dad,daily,damage,damn,dance,danger,dangerous,dare,dark,darkness,data,database,date,daughter,day,dead,deal,dealer,dear,death,debate,debt,decade,decide,decision,declare,decline,decrease,dedicate,deep,deeply,defeat,defend,defense,deficit,define,definitely,definition,degree,delay,delight,deliver,delivery,demand,democracy,democratic,demonstrate,demonstration,density,deny,department,depend,dependent,deposit,depress,depression,depth,derive,describe,description,desert,deserve,design,designer,desire,desk,despite,destroy,destruction,detail,detect,determination,determine,develop,development,device,devote,dialog,die,diet,differ,difference,different,differently,difficult,difficulty,dig,digital,dimension,dinner,direct,direction,directly,director,dirty,disagree,disappear,disappoint,disaster,discipline,discount,discover,discovery,discuss,discussion,disease,dish,disk,dismiss,disorder,display,dispute,distance,distant,distinct,distinction,distinguish,distribute,district,disturb,diversity,divide,division,divorce,do,doctor,document,dog,dollar,domestic,dominate,door,double,doubt,down,dozen,draft,drag,drama,dramatic,dramatically,draw,dream,dress,drink,drive,driver,drop,drug,dry,due,during,dust,duty,each,ear,early,earn,earth,ease,easily,east,eastern,easy,eat,economic,economy,edge,edit,edition,editor,educate,education,educational,effect,effective,effectively,efficiency,efficient,effort,egg,either,elderly,elect,election,electric,electricity,electronic,element,eliminate,else,elsewhere,e-mail,embarrass,embrace,emerge,emergency,emotion,emotional,emphasis,emphasize,empire,employ,employee,employer,employment,empty,enable,encounter,encourage,end,enemy,energy,engage,engine,engineer,enhance,enjoy,enormous,enough,ensure,enter,enterprise,entertain,entertainment,entire,entirely,entitle,entrance,entry,envelope,environment,environmental,episode,equal,equally,equation,equipment,equivalent,era,error,escape,especially,essay,essential,establish,establishment,estate,estimate,ethnic,evaluate,evaluation,even,evening,event,eventually,ever,every,everybody,everyday,everyone,everything,everywhere,evidence,evil,evolution,evolve,exact,exactly,exam,examination,examine,example,exceed,excellent,except,exception,excess,exchange,excite,excitement,exclude,excuse,executive,exercise,exhaust,exhibit,exhibition,exist,existence,expand,expansion,expect,expectation,expenditure,expense,expensive,experience,experiment,experimental,expert,explain,explanation,explore,export,expose,exposure,express,expression,extend,extension,extensive,extent,external,extra,extract,extraordinary,extreme,extremely,eye,face,facility,fact,factor,factory,fade,fail,failure,fair,fairly,faith,faithfully,fall,false,familiar,family,famous,fan,fancy,fantastic,far,farm,farmer,fascinate,fashion,fast,fat,father,fault,favor,favorite,fear,feature,federal,fee,feed,feel,fellow,female,fence,festival,few,fiction,field,fifteen,fifty,fight,figure,file,fill,film,filter,final,finally,finance,financial,find,fine,finger,finish,fire,firm,firmly,first,firstly,fish,fit,fix,flag,flash,flat,flexible,flight,float,flood,floor,flow,flower,fly,focus,fold,folk,follow,food,fool,foot,football,for,force,forecast,foreign,forest,forever,forget,form,formal,format,formation,former,formula,forth,fortunate,fortune,forward,found,foundation,fragment,frame,framework,free,freedom,freeze,frequency,frequent,frequently,fresh,friend,friendly,friendship,frighten,from,front,fruit,fuel,fulfill,full,fully,fun,function,functional,fund,fundamental,funny,furniture,further,furthermore,future,gain,gallery,game,gap,garden,gas,gate,gather,gay,gaze,gear,gender,gene,general,generally,generate,generation,genetic,gentle,gentleman,gently,genuine,gesture,get,giant,gift,girl,give,glad,glance,glass,global,go,goal,god,gold,golden,golf,good,govern,government,governor,grab,grade,gradually,graduate,grain,grammar,grand,grandmother,grant,grass,grateful,gray,great,greatly,green,greet,grin,ground,group,grow,growth,guarantee,guard,guess,guest,guide,guideline,guilty,guitar,gun,guy,habit,hair,half,hall,hand,handle,hang,happen,happiness,happy,harbor,hard,hardly,harm,hat,hate,have,he,head,health,healthy,hear,heart,heat,heavily,heavy,height,hell,hello,help,helpful,hence,her,here,hero,herself,hesitate,hi,hide,high,highlight,highly,hill,him,himself,hint,hire,his,historian,historic,historical,history,hit,hold,holder,hole,holiday,home,honest,honor,hook,hope,hopefully,horrible,horse,hospital,host,hot,hotel,hour,house,household,how,however,huge,human,humor,hunger,hunt,hurry,hurt,husband,hypothesis,I,ice,idea,ideal,identify,identity,if,ignore,ill,illegal,illness,illustrate,illustration,image,imagination,imagine,immediate,immediately,immigrant,implement,implementation,implication,imply,import,importance,important,impose,impossible,impress,impression,impressive,improve,improvement,in,incentive,inch,incident,include,income,incorporate,increase,increasingly,indeed,independence,independent,index,indicate,indication,individual,industrial,industry,infant,infection,inflation,influence,inform,information,initial,initially,initiative,injure,injury,inner,innocent,innovation,input,inquiry,inside,insight,insist,inspire,install,instance,instead,institution,institutional,instruction,instrument,insurance,insure,integrate,intellectual,intelligence,intend,intense,intention,interaction,interest,interior,internal,international,interpret,interpretation,intervention,interview,into,introduce,introduction,invent,invest,investigate,investigation,investment,investor,invitation,invite,involve,involvement,iron,island,isolate,issue,it,item,its,itself,jacket,jail,job,join,joint,joke,journal,journalist,journey,joy,judge,judgment,jump,jury,just,justice,justify,keen,keep,key,kick,kid,kill,kind,king,kiss,kitchen,knee,knife,knock,know,knowledge,label,labor,laboratory,lack,lady,lake,land,landscape,language,large,largely,last,late,latter,laugh,laughter,launch,law,lawyer,lay,layer,lazy,lead,leader,leadership,league,lean,leap,learn,least,leather,leave,lecture,left,leg,legal,legislation,lend,length,less,lesson,let,letter,level,liability,liberal,library,license,lie,life,lift,light,like,likely,limit,limitation,line,link,lip,liquid,list,listen,listener,literally,literary,literature,little,live,load,loan,local,locate,location,lock,log,logic,long,look,loose,lose,loss,lot,loud,love,lovely,lover,low,luck,lucky,lunch,luxury,machine,mad,magazine,magic,mail,main,mainly,maintain,maintenance,major,majority,make,maker,male,man,manage,management,manager,manner,manufacture,manufacturer,many,map,march,margin,mark,market,marriage,marry,mass,massive,master,match,mate,material,mathematics,matter,mature,maximum,may,maybe,mayor,me,meal,mean,meanwhile,measure,measurement,meat,mechanism,medical,medicine,medium,meet,member,membership,memory,mental,mention,menu,mere,merely,mess,message,metal,meter,method,middle,might,mile,military,milk,mind,mine,minimum,minister,minor,minority,minute,mirror,miss,mission,mistake,mix,mixture,mobile,mode,model,moderate,modern,modify,module,mom,moment,money,monitor,month,monthly,mood,moon,moral,more,moreover,morning,mortgage,most,mostly,mother,motion,motivate,motivation,motor,mount,mountain,mouse,mouth,move,movement,movie,much,multiple,murder,muscle,museum,music,musical,musician,must,mutual,my,myself,mystery,name,narrative,narrow,nation,national,native,natural,naturally,nature,near,nearby,nearly,necessarily,necessary,neck,need,negative,neglect,negotiate,negotiation,neighbor,neighborhood,neither,nerve,nervous,net,network,never,nevertheless,new,newly,news,newspaper,next,nice,night,no,nobody,noise,none,nor,normal,normally,north,northern,nose,not,note,nothing,notice,notion,noun,novel,now,nowadays,nowhere,nuclear,number,numerous,nurse,object,objective,obligation,observation,observe,obvious,obviously,occasion,occasionally,occupy,occur,ocean,odd,of,off,offense,offer,office,officer,official,often,oil,okay,old,on,once,one,online,only,onto,open,opera,operate,operation,operator,opinion,opponent,opportunity,oppose,opposite,opposition,option,or,orange,order,ordinary,organic,organization,organize,origin,original,originally,other,otherwise,ought,our,ourselves,out,outcome,outline,output,outside,over,overall,overcome,overseas,owe,own,owner,ownership,pace,pack,package,page,pain,paint,pair,pale,panel,panic,paper,paragraph,parallel,parent,park,part,participant,participate,participation,particular,particularly,partly,partner,partnership,party,pass,passage,passenger,passion,past,path,patient,pattern,pause,pay,payment,peace,peak,peer,pen,penalty,pension,people,per,perceive,percent,percentage,perception,perfect,perfectly,perform,performance,perhaps,period,permanent,permission,permit,person,personal,personality,personally,personnel,perspective,persuade,phase,phenomenon,philosophy,phone,photo,photograph,phrase,physical,piano,pick,picture,piece,pig,pile,pilot,pink,pipe,pitch,place,plain,plan,plane,planet,plant,plastic,plate,platform,play,player,pleasant,please,pleasure,plenty,plot,plus,pocket,poem,poet,poetry,point,police,policy,political,politician,politics,poll,pollution,pool,poor,pop,popular,population,port,portion,portrait,pose,position,positive,possess,possession,possibility,possible,possibly,post,pot,potato,potential,potentially,pound,pour,poverty,power,powerful,practical,practice,praise,pray,precise,precisely,predict,prefer,preference,pregnancy,pregnant,premise,preparation,prepare,presence,present,presentation,preserve,president,presidential,press,pressure,presumably,pretend,pretty,prevent,previous,previously,price,pride,primarily,primary,prime,principal,principle,print,printer,prior,priority,prison,prisoner,private,privilege,prize,pro,probability,probably,problem,procedure,proceed,process,produce,producer,product,production,profession,professional,professor,profile,profit,program,progress,project,promise,promote,promotion,prompt,proof,proper,properly,property,proportion,proposal,propose,prospect,protect,protection,protein,protest,proud,prove,provide,province,provision,psychological,pub,public,publication,publisher,pull,pump,pupil,purchase,pure,purpose,pursue,push,put,qualification,qualify,quality,quantity,quarter,question,quick,quickly,quiet,quietly,quite,quote,race,racial,radical,radio,rail,rain,raise,random,range,rank,rapid,rapidly,rare,rarely,rat,rate,rather,ratio,raw,reach,react,reaction,read,reader,ready,real,reality,realize,really,rear,reason,reasonable,reasonably,recall,receive,recent,recently,reckon,recognition,recognize,recommend,recommendation,record,recover,recovery,recruit,red,reduce,reduction,refer,reference,reflect,reflection,reform,refugee,refuse,regard,regardless,region,regional,register,registration,regret,regular,regularly,regulate,regulation,reject,relate,relation,relationship,relative,relatively,relax,release,relevant,reliable,relief,religion,religious,rely,remain,remark,remarkable,remember,remind,remote,remove,rent,repair,repeat,replace,reply,report,reporter,represent,representation,representative,reputation,request,require,requirement,rescue,research,researcher,reserve,resident,resign,resist,resistance,resolution,resolve,resort,resource,respect,respectively,respond,response,responsibility,responsible,rest,restaurant,restore,restrict,restriction,result,retail,retain,retire,retirement,return,reveal,revenue,reverse,review,revise,revolution,reward,rice,rich,rid,ride,right,ring,rise,risk,rival,river,road,rock,role,roll,romantic,roof,room,root,rough,roughly,round,route,routine,row,royal,ruin,rule,run,rural,rush,sad,safe,safety,sail,sake,salary,sale,salt,same,sample,sanction,sand,satisfaction,satisfy,save,say,scale,scan,scare,scene,schedule,scheme,scholar,school,science,scientific,scientist,scope,score,scream,screen,sea,seal,search,season,seat,second,secondary,secondly,secret,secretary,section,sector,secure,security,see,seed,seek,seem,segment,select,selection,self,sell,send,senior,sense,sensitive,sentence,separate,sequence,series,serious,seriously,servant,serve,server,service,session,set,settle,settlement,several,severe,sex,sexual,shade,shadow,shake,shall,shape,share,shareholder,sharp,she,sheep,sheet,shelf,shell,shelter,shift,shine,ship,shirt,shock,shoe,shoot,shop,shore,short,shot,should,shoulder,shout,show,shower,shut,sick,side,sigh,sight,sign,signal,significance,significant,significantly,silence,silent,silly,silver,similar,similarly,simple,simply,since,sing,singer,single,sink,sir,sister,sit,site,situate,situation,size,ski,skill,skin,skirt,sky,slave,sleep,slice,slide,slight,slightly,slip,slope,slow,slowly,small,smart,smell,smile,smoke,smooth,snap,snow,so,social,society,soft,software,soil,soldier,solid,solution,solve,some,somebody,somehow,someone,something,sometimes,somewhat,somewhere,son,song,soon,sorry,sort,soul,sound,source,south,southern,space,spare,speak,speaker,special,specialist,specialize,species,specific,specifically,specify,speech,speed,spell,spend,spin,spirit,split,sponsor,sport,spot,spread,spring,square,stability,stable,staff,stage,stain,stair,stake,stamp,stand,standard,star,stare,start,state,statement,station,statistic,status,stay,steady,steal,steel,stem,step,stick,still,stimulate,stir,stock,stomach,stone,stop,storage,store,storm,story,straight,strain,strange,stranger,strategy,stream,street,strength,strengthen,stress,stretch,strict,strike,string,strip,stroke,strong,strongly,structural,structure,struggle,student,studio,study,stuff,stupid,style,subject,submit,subsequent,subsequently,substance,substantial,substitute,succeed,success,successful,successfully,such,sudden,suddenly,suffer,sufficient,sugar,suggest,suggestion,suit,suitable,sum,summarize,summary,summer,sun,supplement,supplier,supply,support,supporter,suppose,sure,surely,surface,surgery,surprise,surprisingly,surround,survey,survival,survive,suspect,suspend,sustain,swear,sweep,sweet,swim,swing,switch,symbol,symptom,system,table,tackle,tail,take,tale,talent,talk,tall,tank,tap,tape,target,task,taste,tax,taxi,tea,teach,teacher,team,tear,technical,technique,technology,teenager,telephone,television,tell,temperature,temporary,tend,tendency,tender,tennis,tension,tent,term,terrible,territory,terrorist,test,text,than,thank,that,the,theater,their,them,theme,themselves,then,theoretical,theory,therapy,there,therefore,these,they,thick,thin,thing,think,thirst,this,those,though,threat,threaten,throat,through,throughout,throw,thus,ticket,tie,tight,till,time,tiny,tip,tire,tissue,title,to,today,together,tomorrow,tone,tongue,tonight,too,tool,tooth,top,topic,total,totally,touch,tough,tour,tourism,tourist,toward,tower,town,toy,trace,track,trade,tradition,traditional,traffic,trail,train,transfer,transform,transition,translate,transport,transportation,trap,travel,treat,treatment,tree,trend,trial,trick,trigger,trip,troop,trouble,truck,true,truly,trust,truth,try,tube,tune,turn,twice,twin,twist,type,typical,typically,ugly,ultimately,unable,uncertainty,uncle,unclear,under,undergo,underlie,understand,undertake,unemployment,unfortunately,uniform,union,unique,unit,unite,universal,universe,university,unknown,unless,unlike,unlikely,until,unusual,up,update,upon,upper,upset,urban,urge,us,use,useful,user,usual,usually,valley,valuable,value,van,variable,variation,variety,various,vary,vast,vegetable,vehicle,venture,verb,version,versus,very,vessel,veteran,via,vice,victim,victory,video,view,village,violence,violent,virtually,virus,visible,vision,visit,visitor,visual,vital,voice,volume,voluntary,volunteer,vote,voter,wage,wait,wake,walk,wall,wander,want,war,warm,warn,wash,waste,watch,water,wave,way,we,weak,weakness,wealth,wealthy,weapon,wear,weather,web,wed,week,weekend,weekly,weigh,weight,weird,welcome,welfare,well,west,western,wet,what,whatever,wheel,when,whenever,where,whereas,wherever,whether,which,while,whilst,whisper,white,who,whole,whom,whose,why,wide,widely,wife,wild,will,win,wind,window,wine,wing,winner,winter,wipe,wire,wise,wish,with,withdraw,within,without,witness,woman,wonder,wonderful,wood,wooden,word,work,worker,world,worry,worth,would,wound,wrap,write,writer,wrong,yard,year,yellow,yes,yesterday,yet,yield,you,young,your,yourself,youth,zero,zone";
	private List<String> defaultReplace = new ArrayList<>();


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Parameters
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// Data
	private List<List<Tag>> tags = new ArrayList<>();

	private Map<String, Long> tagsFreq = new HashMap<>();
	private Map<String, Double> vocabPre = new HashMap<>();
	private Map<String, Double> vocabPost = new HashMap<>();

	private List<String> whitelistWords = new ArrayList<>();
	private List<String> whitelistGroups = new ArrayList<>();
	private List<String> whitelistVocab = new ArrayList<>();
	private List<String> blacklist = new ArrayList<>();

	// Initialize pipeline steps
	// The index selects the working copy. 0 = original
	private Preprocess preprocess;
	private Spellcorrect spellcorrect;
	private Composite composite;
	private Postprocess postprocess;


	public Workflow() {
		for (int i = 0; i < 5; i++) {
			tags.add(new ArrayList<>());
		}

		defaultReplace.add("-, ");
		defaultReplace.add("_, ");
		defaultReplace.add(":, ");
		defaultReplace.add(";, ");
		defaultReplace.add("/, ");
		defaultReplace.add("(, ");
		defaultReplace.add("), ");
		defaultReplace.add("[, ");
		defaultReplace.add("], ");
		defaultReplace.add("{, ");
		defaultReplace.add("}, ");

		preprocess = new Preprocess(blacklist);
		spellcorrect = new Spellcorrect(whitelistWords, whitelistGroups, whitelistVocab);
		composite = new Composite(whitelistGroups);
		postprocess = new Postprocess();
	}

	public void computeWorkflow(SocketIOClient client)
	{
		System.out.println("computeWorkflow");
		if(!running)
		{
			running = true;
		}

		if(preDirty)
		{
			client.sendEvent("computePre", "started");
			client.sendEvent("computeSpell", "started");
			client.sendEvent("computeComp", "started");
			computePreprocessing(client);
			client.sendEvent("computePre", "finished");

			preDirty = false;
			spellDirty = true;
		}

		if(spellDirty)
		{
			client.sendEvent("computeSpell", "started");
			client.sendEvent("computeComp", "started");
			computeSpellCorrect(client);
			client.sendEvent("computeSpell", "finished");

			spellDirty = false;
			compDirty = true;
		}

		if(compDirty)
		{
			client.sendEvent("computeComp", "started");
			computeGroups(client);
			client.sendEvent("computeComp", "finished");

			compDirty = false;
		}
	}

	public void sendParams(SocketIOClient client) {
		// Preprocessing
		client.sendEvent("preFilterParams", sendPreFilterParams());
		client.sendEvent("preRemoveParams", sendPreRemoveParams());
		client.sendEvent("preReplaceParams", sendPreReplaceParams());
		client.sendEvent("preDictionaryParams", sendPreDictionaryParams());

		// Spell correct
		client.sendEvent("spellImportance", sendSpellImportanceParams());
		client.sendEvent("spellSimilarity", sendSpellSimilarityParams());
		client.sendEvent("spellMinWordSize", sendSpellMinWordSizeParams());
		client.sendEvent("spellDictionaryParams", sendSpellDictionaryParams());

		// Composite
		client.sendEvent("compFrequentParams", sendCompFrequentParams());
		client.sendEvent("compUniqueParams", sendCompUniqueParams());
		client.sendEvent("compSizeParams", sendCompSizeParams());
		client.sendEvent("compOccParams", sendCompOccParams());
		client.sendEvent("compSplitParams", sendCompSplitParams());

		// Postprocess
		client.sendEvent("postFilterParams", sendPostFilterParams());
		client.sendEvent("postAllParams", sendPostAllParams());
		client.sendEvent("postReplaceParams", sendPostReplaceParams());
		client.sendEvent("postRemoveParams", sendPostRemoveParams());
		client.sendEvent("postLengthParams", sendPostLengthParams());
		client.sendEvent("postSplitParams", sendPostSplitParams());
	}

	public void sendData(SocketIOClient client)
	{
		// Send Pre data
		client.sendEvent("preFilterData", sendPreFilterHistogram());
		client.sendEvent("preFilterGrid", sendPreFilter());

		// Send Spell data
		client.sendEvent("similarities", sendSimilarityHistogram());
		client.sendEvent("vocab", sendVocab());
		client.sendEvent("importance", sendPreVocabHistogram());

		// Send Composite data
		client.sendEvent("frequentGroups", sendFrequentGroups());
		client.sendEvent("frequentData", sendFrequentHistogram());
		client.sendEvent("uniqueGroups", sendUniqueGroups());
		client.sendEvent("uniqueData", sendUniqueHistogram());

		// Send Post data
		client.sendEvent("postFilterGrid", sendPostVocab());
		client.sendEvent("postFilterData", sendPostVocabHistogram());
		client.sendEvent("output", sendOverview(3));
		client.sendEvent("outputState", "Multiword Tags");

		// Send Final data
		client.sendEvent("postImportantWords", sendPostImportant());

	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Load data - Dataset 0
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void applyImportedData(String json) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		String item, name, weight;

		if (packages < count) {
			packages++;

			for (Map<String, Object> aMap : map) {
				try {
					// Starting ID with 1 to be database ready
					globalID++;

					item = String.valueOf(aMap.get("item"));
					name = String.valueOf(aMap.get("tag"));
					weight = String.valueOf(aMap.get("weight"));

					tags.get(0).add(new Tag(globalID, item, name.toLowerCase().replaceAll(" +"," "), Double.parseDouble(weight), 0, 0));
				} catch (Exception e) {
					System.out.println(aMap);
				}
			}
		}
	}
	
	public void applyImportedDataFinished(SocketIOClient client) {
		// Compute word frequency
		help.wordFrequency(tags.get(0), tagsFreq);

		// Send that the data is loaded
		dataLoaded = true;
		client.sendEvent("dataLoaded", sendDataLoaded());
		System.out.println("DataLoaded");
	}
	
	public void applyImportedDataCount(int count, SocketIOClient client) {
		tags.forEach(List<Tag>::clear);

		globalID = 0;
		
		this.count = count;
		this.packages = 0;

		running = false;
		client.sendEvent("isRunning",sendStatus());
	}

	public void selectMode(String data, SocketIOClient client) {

		if(data.equals("guided"))
		{
			client.sendEvent("initRunning","started");

			if(!running) applyDefaults();

			mode = data;
			client.sendEvent("selectedMode",sendMode());

			// Wait a little bit for the angular ng-if statement
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");

			if(!running) preDirty = true;

			computeWorkflow(client);
		}

		if(data.equals("free") || data.equals("linked"))
		{
			client.sendEvent("initRunning","started");

			mode = data;
			client.sendEvent("selectedMode",sendMode());

			// Wait a little bit for the angular ng-if statement
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");

			if(!running) preDirty = true;

			computeWorkflow(client);
		}

		if(data.equals("reconnect"))
		{
			client.sendEvent("initRunning","started");

			// Mode stays
			client.sendEvent("selectedMode",sendMode());

			// Wait a little bit for the angular ng-if statement
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			sendParams(client);
			sendData(client);

			client.sendEvent("initRunning","finished");
		}

		if(!running)
		{
			running = true;
		}
	}

	private void applyDefaults() {
		// Preprocessing
		preprocess.setDefaultReplace(defaultReplace);

		preprocess.setRemove("'");

		blacklist.clear();
		Collections.addAll(blacklist, stopwords.split(","));

		whitelistWords.clear();
		Collections.addAll(whitelistWords, english.split(","));

		// Spell correct
		spellcorrect.setSpellImportance(0.7);
		spellcorrect.setSpellSimilarity(0.7);
		spellcorrect.setMinWordSize(3);

		// Composite
		composite.setFrequentThreshold(0.35);
		composite.setJaccardThreshold(0.7);

		// Postprocessing
		postprocess.setPostFilter(0.25);
	}
	
	public String sendStatus() {
		return running.toString();
	}

	public String sendMode() {
		return mode;
	}

	public String sendDataLoaded() {
		return dataLoaded.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Preprocessing - Dataset 1
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computePreprocessing(SocketIOClient client) {
		help.resetStep(tags, 1);

		// Remove tags
		preprocess.applyFilter(tags.get(1), tagsFreq);
		
		// Remove characters
		preprocess.removeCharacters(tags.get(1));
		
		// Replace characters
		preprocess.replaceCharacters(tags.get(1));

		// Remove blacklisted words
		help.removeBlacklistedWords(tags.get(1), blacklist);

		// Create preFilter vocab
		weighting.vocab(tags.get(1), vocabPre);

		// Cluster words for further use
		spellcorrect.clustering(tags.get(1), vocabPre, whitelistVocab);
		
		client.sendEvent("similarities", sendSimilarityHistogram());
		client.sendEvent("vocab", sendVocab());
		client.sendEvent("importance", sendPreVocabHistogram());
	}
	
	// Apply changes
	public void applyPreFilter(int threshold, SocketIOClient client) {
		// Set threshold
		preprocess.setFilter(threshold);

		preDirty = true;
	}
	
	public void applyPreRemove(String chars, SocketIOClient client) {
		// Set characters for removal
		preprocess.setRemove(chars);

		preDirty = true;
	}
	
	public void applyPreReplace(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setReplace(map);

		preDirty = true;
	}
	
	public void applyPreDictionary(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		
		preprocess.setDictionary(map);

		preDirty = true;
	}
	
	// Send Params
	public double sendPreFilterParams() {
		return preprocess.getFilter();
	}
	
	public String sendPreRemoveParams() {
		return preprocess.getRemove();
	}
	
	public List<String> sendPreReplaceParams() {
		return preprocess.getReplace();
	}
	
	public List<String> sendPreDictionaryParams() {
		return blacklist;
	}
	
	// Send Data
	public String sendPreFilter() {
		return help.objectToJsonString(preprocess.preparePreFilter(tagsFreq));
	}
	
	public String sendPreFilterHistogram() {
		return help.objectToJsonString(preprocess.preparePreFilterHistogram(tagsFreq));
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Spell Correction - Dataset 2
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeSpellCorrect(SocketIOClient client) {
		// Reset current stage
		help.resetStep(tags, 2);

		// Apply clustering
		spellcorrect.applyClustering(tags.get(2), vocabPre);
		
		// Compute further data
		composite.group(tags.get(2));
		
		client.sendEvent("frequentGroups", sendFrequentGroups());
		client.sendEvent("frequentData", sendFrequentHistogram());
		client.sendEvent("uniqueGroups", sendUniqueGroups());
		client.sendEvent("uniqueData", sendUniqueHistogram());
		client.sendEvent("replacements", sendReplacements());
	}
	
	// Apply changes
	public void applySpellCorrect(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);
		double imp, sim;

		if(map.get(0).get("importance").equals(1) || map.get(0).get("importance").equals(0))
		{
			imp = ((Integer) map.get(0).get("importance"));
		}
		else
		{
			imp = ((Double) map.get(0).get("importance"));
		}

		if(map.get(0).get("similarity").equals(1) || map.get(0).get("similarity").equals(0))
		{
			sim = ((Integer) map.get(0).get("similarity"));
		}
		else
		{
			sim = ((Double) map.get(0).get("similarity"));
		}

		spellcorrect.setSpellImportance(imp);
		spellcorrect.setSpellSimilarity(sim);

		spellDirty = true;
	}

	public void applySpellImport(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		spellcorrect.setDictionary(map);

		spellDirty = true;
	}

	public void applySpellMinWordSize(int minWordSize, SocketIOClient client) {
		// Set minWordSize
		spellcorrect.setMinWordSize(minWordSize);

		spellDirty = true;
	}

	// Send Params
	public double sendSpellImportanceParams() {
		return spellcorrect.getSpellImportance();
	}
	
	public double sendSpellSimilarityParams() {
		return spellcorrect.getSpellSimilarity();
	}
	
	public int sendSpellMinWordSizeParams() {
		return spellcorrect.getMinWordSize();
	}

	public List<String> sendSpellDictionaryParams() {
		List<String> temp = new ArrayList<>();

		temp.addAll(whitelistWords);
		temp.addAll(whitelistGroups);

		return temp;
	}

	// Send Data
	public String sendCluster(String tag) {
		return help.objectToJsonString(spellcorrect.prepareCluster(tag, vocabPre));
	}
	
	public String sendSimilarityHistogram() {
		return help.objectToJsonString(spellcorrect.prepareSimilarityHistogram());
	}
	
	public int sendReplacements(String json) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		double imp, sim;

		if(map.get(0).get("importance").equals(0)) return 0;

		if(map.get(0).get("importance").equals(1))
		{
			imp = ((Integer) map.get(0).get("importance"));
		}
		else
		{
			imp = ((Double) map.get(0).get("importance"));
		}

		sim = getSim(map);

		return spellcorrect.prepareReplacements(sim, imp, vocabPre);
	}

	private double getSim(List<Map<String, Object>> map) {
		double sim;
		if(map.get(0).get("similarity").equals(0))
		{
			sim = ((Integer) map.get(0).get("similarity"));
		}
		else if(map.get(0).get("similarity").equals(1))
		{
			sim = ((Integer) map.get(0).get("similarity"));
		}
		else
		{
			sim = ((Double) map.get(0).get("similarity"));
		}
		return sim;
	}

	public String sendReplacementData(String json)
	{
		List<Map<String, Object>> map = help.jsonStringToList(json);

		double imp, sim;

		if(map.get(0).get("importance").equals(0)) return "";

		if(map.get(0).get("importance").equals(1))
		{
			imp = ((Integer) map.get(0).get("importance"));
		}
		else
		{
			imp = ((Double) map.get(0).get("importance"));
		}

		sim = getSim(map);

		return help.objectToJsonString(spellcorrect.prepareReplacementData(sim, imp, vocabPre));
	}
	
	public int sendReplacements() {
		double sim = spellcorrect.getSpellSimilarity();
		double imp = spellcorrect.getSpellImportance();

		return spellcorrect.prepareReplacements(sim, imp, vocabPre);
	}

	public String sendVocab() {
		return help.objectToJsonString(help.prepareVocab(vocabPre));
	}

	public String sendPreVocabHistogram() {
		return help.objectToJsonString(help.prepareVocabHistogram(vocabPre));
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Composites - Dataset 3
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void computeGroups(SocketIOClient client) {
		help.resetStep(tags, 3);
		
		// Compute all word groups
		composite.applyGroups(tags.get(3));

		// Provide further data
		weighting.vocab(tags.get(3), vocabPost);

		client.sendEvent("postFilterGrid", sendPostVocab());
		client.sendEvent("postFilterData", sendPostVocabHistogram());

		client.sendEvent("output", sendOverview(3));
		client.sendEvent("outputState", "Multiword Tags");

		prepareSalvaging(client);
	}
	
	// Apply changes
	public void applyCompositeFrequent(double threshold, SocketIOClient client) {
		// Set threshold
		composite.setFrequentThreshold(threshold);
		
		compDirty = true;
	}
	
	public void applyCompositeUnique(double threshold, SocketIOClient client) {
		// Set threshold
		composite.setJaccardThreshold(threshold);

		compDirty = true;
	}
	
	public void applyCompositeParams(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		composite.setMaxGroupSize((Integer) map.get(0).get("maxGroupSize"));
		composite.setMinOccurrence((Integer) map.get(0).get("minOcc"));
		composite.setSplit((Boolean) map.get(0).get("split"));

		compDirty = true;
	}
	
	// Send Params
	public double sendCompFrequentParams() {
		return composite.getFrequentThreshold();
	}
	
	public double sendCompUniqueParams() {
		return composite.getJaccardThreshold();
	}
	
	public int sendCompSizeParams() {
		return composite.getMaxGroupSize();
	}
	
	public int sendCompOccParams() {
		return composite.getMinOccurrence();
	}

	public Boolean sendCompSplitParams() {
		return composite.getSplit();
	}

	// Send Data
	public String sendFrequentGroups() {
		return help.objectToJsonString(composite.prepareFrequentGroups());
	}
	
	public String sendUniqueGroups() {
		return help.objectToJsonString(composite.prepareUniqueGroups());
	}
	
	public String sendFrequentHistogram() {
		return help.objectToJsonString(composite.prepareFrequentHistogram());
	}
	
	public String sendUniqueHistogram() {
		return help.objectToJsonString(composite.prepareUniqueHistogram());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Postprocessing - Dataset 4
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void prepareSalvaging(SocketIOClient client) {
		postprocess.updateImportantWords(vocabPost);
		postprocess.updateSalvageWords();
		
		client.sendEvent("postImportantWords", sendPostImportant());
	}
	
	public void computeSalvaging(SocketIOClient client) {
		client.sendEvent("postSalvaging", "true");
		postprocess.computeSalvaging(vocabPost);
		client.sendEvent("postSalvaging", "false");

		client.sendEvent("postSalvageData", sendPostSalvageData());
	}
	
	// Apply changes
	public void applySalvaging(SocketIOClient client) {
		help.resetStep(tags, 4);
		
		client.sendEvent("computePost", "started");

		computeSalvaging(client);
		postprocess.applySalvaging(tags.get(4));

		client.sendEvent("computePost", "finished");

		client.sendEvent("output", sendOverview(4));
		client.sendEvent("outputState", "Finalize");
	}
	
	public void applyPostFilter(double threshold, SocketIOClient client) {
		// Set threshold
		postprocess.setPostFilter(threshold);

		postprocess.updateImportantWords(vocabPost);

		client.sendEvent("postImportantWords", sendPostImportant());

		postprocess.updateSalvageWords();

		computeSalvaging(client);
	}
	
	public void applyPostReplace(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		postprocess.setPostReplace(map);

		postprocess.updateSalvageWords();

		computeSalvaging(client);
	}

	public void applyPostRemove(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		postprocess.setPostRemove(map);

		postprocess.updateSalvageWords();

		computeSalvaging(client);
	}

	public void applyPostParams(String json, SocketIOClient client) {
		List<Map<String, Object>> map = help.jsonStringToList(json);

		postprocess.setMinWordLength((Integer) map.get(0).get("minWordLength"));
		postprocess.setSplitTags((Boolean) map.get(0).get("split"));
		postprocess.setUseAllWords((Boolean) map.get(0).get("useAll"));

		computeSalvaging(client);
	}
	
	// Send Params
	public double sendPostFilterParams() {
		return postprocess.getPostFilter();
	}
	
	public int sendPostLengthParams() {
		return postprocess.getMinWordLength();
	}
	
	public Boolean sendPostAllParams() {
		return postprocess.getUseAllWords();
	}
	
	public List<String> sendPostReplaceParams() {
		return postprocess.getPostReplace();
	}

	public List<String> sendPostRemoveParams() {
		return postprocess.getPostRemove();
	}

	public Boolean sendPostSplitParams() {
		return postprocess.getSplitTags();
	}
	
	// Send Data
	public String sendPostVocab() {
		return help.objectToJsonString(help.prepareVocab(vocabPost));
	}
	
	public String sendPostVocabHistogram() {
		return help.objectToJsonString(help.prepareVocabHistogram(vocabPost));
	}
	
	public String sendPostImportant() {
		return help.objectToJsonString(postprocess.prepareImportantWords());
	}

	public String sendPostSalvageData() {
		return help.objectToJsonString(postprocess.prepareSalvagedData());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Header
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Overview
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String sendOverview(int index) {
		// Each send to the overview sets the history provider correctly
		lastAppliedStep = index;

		// Send history data
		Supplier<List<gridOverview>> supplier = ArrayList::new;

		List<gridOverview> tags_filtered = tags.get(index).stream()
				.map(p -> new gridOverview(p.getTag(), p.getItem(), p.getWeight(), p.getChanged()))
				.collect(Collectors.toCollection(supplier));

		return help.objectToJsonString(tags_filtered);
	}

	public String sendHistory(String json) {
		List<gridHistory> tags_filtered = new ArrayList<>();

		List<Map<String, Object>> map = help.jsonStringToList(json);

		String tag = (String) map.get(0).get("tag");
		String item = (String) map.get(0).get("item");
		int id = 0;
		List<String> temp = new ArrayList<>(5);

		temp.add("");
		temp.add("");
		temp.add("");
		temp.add("");
		temp.add("");

		// Get ID
		for(Tag t: tags.get(lastAppliedStep))
		{
			if(t.getTag().equals(tag) && t.getItem().equals(item))
			{
				id = t.getId();
			}
		}

		for(int i = 0; i < 5; i++)
		{
			for(Tag t: tags.get(i))
			{
				if(t.getId() == id)
				{
					temp.set(i, t.getTag());
				}
			}
		}

		tags_filtered.add(new gridHistory(temp));

	    return help.objectToJsonString(tags_filtered);
	}
}
