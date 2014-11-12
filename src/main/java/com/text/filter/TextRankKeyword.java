package com.text.filter;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import java.io.IOException;
import java.util.*;
/*
 * 提取文本中的关键词，是受到网页之间关系PageRank算法启发，利用局部词汇之间关系（共现窗口）对后续关键词进行排序
 */
public class TextRankKeyword
{
    public static final int nKeyword = 10;
 
    static final float d = 0.85f;
  
    static final int max_iter = 200;
    static final float min_diff = 0.001f;

    public TextRankKeyword()
    {
        // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public String getKeyword(String title, String content)
    {
        List<Term> termList = ToAnalysis.parse(title + content);
//        System.out.println(termList);
        List<String> wordList = new ArrayList<String>();
        for (Term t : termList)
        {
            if (shouldInclude(t))
            {
                wordList.add(t.getName());
            }
        }
//        System.out.println(wordList);
        Map<String, Set<String>> words = new HashMap<String, Set<String>>();
        Queue<String> que = new LinkedList<String>();
        for (String w : wordList)
        {
            if (!words.containsKey(w))
            {
                words.put(w, new HashSet<String>());
            }
            que.offer(w);
            if (que.size() > 5)
            {
                que.poll();
            }

            for (String w1 : que)
            {
                for (String w2 : que)
                {
                    if (w1.equals(w2))
                    {
                        continue;
                    }

                    words.get(w1).add(w2);
                    words.get(w2).add(w1);
                }
            }
        }
//        System.out.println(words);
        Map<String, Float> score = new HashMap<String, Float>();
        for (int i = 0; i < max_iter; ++i)
        {
            Map<String, Float> m = new HashMap<String, Float>();
            float max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : words.entrySet())
            {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                m.put(key, 1 - d);
                for (String other : value)
                {
                    int size = words.get(other).size();
                    if (key.equals(other) || size == 0) continue;
                    m.put(key, m.get(key) + d / size * (score.get(other) == null ? 0 : score.get(other)));
                }
                max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
            }
            score = m;
            if (max_diff <= min_diff) break;
        }
        List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(score.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Float>>()
        {
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2)
            {
                return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
            }
        });
//        System.out.println(entryList);
        String result = "";
        for (int i = 0; i < nKeyword; ++i)
        {
            result += entryList.get(i).getKey() + ':' + entryList.get(i).getValue() + "#";
        }
        return result;
    }

    public static void main(String[] args) throws IOException
    {
//    	String content = "";
//        FileReader fr=new FileReader("part-m-00000");
//        BufferedReader br=new BufferedReader(fr);
//        String s = br.readLine();
//        while(s!=null){
////            System.out.println(s);
//            content = content + s;
//            s=br.readLine();
//        }
//        br.close();
        String content = "女士,毛器,男士 女士剃毛器与男士的有什么区别其实功能都是差不多的，都是剃除多余毛发，只是女士剃毛器是专门针对女士，为女士设计的清洁用具，美观大方，小巧轻便，携带容易，功能比较齐全，包装精美。让爱美的女性及时拥有她.保 女士剃毛器与男士的有什么区别_百度知道剃毛器和剃须刀什么区别的说？手机京东提供羽绒服的最新报价、促销、评论、导购、图片等相关信息  羽绒服 - 京东手机版手机京东提供羽绒服的最新报价、促销、评论、导购、图片等相关信息  羽绒服 - 京东手机版百度百科,百科手机版,中文百科 百度百科是一部内容开放、自由的网络百科全书，旨在创造一个涵盖所有领域知识、服务所有互联网用户的中文知识性百科全书。 在这里你可以参与词条编辑，分享贡献你的知识。 剃毛器 - 百度百科京东网上商城 - 百度酷派商城-酷派官方网上商城，世界杯剃须刀,女用,毛器,男式 女用剃毛器跟男式剃须刀有什么区别其实功能都是差不多的，都是剃除多余毛发，只是女士剃毛器是专门针对女士，为女士设计的清洁用具，美观大方，小巧轻便，携带容易，功能比较齐全，包装精美。让爱美的女性及时拥有她 女用剃毛器跟男式剃须刀有什么区别_百度知道电动剃须刀,牌子 电动剃须刀哪个牌子的好？谁知道啊也只有国产飞利浦性价比不错. 电动剃须刀哪个牌子的好？谁知道啊_百度知道";
        System.out.println(new TextRankKeyword().getKeyword("", content));

    }

    class Document
    {
        Map<String, Set<String>> words;
        Map<String, Float> score;
    }

  
    public boolean shouldInclude(Term term)
    {
        if (
                term.getNatrue().natureStr.startsWith("n") ||
                term.getNatrue().natureStr.startsWith("v") ||
                term.getNatrue().natureStr.startsWith("d") ||
                term.getNatrue().natureStr.startsWith("a")
                )
        {
            // TODO 浣犻渶瑕佽嚜宸卞疄鐜颁竴涓仠鐢ㄨ瘝琛�
//            if (!StopWordDictionary.contains(term.getName()))
//            {
                return true;
//            }
        }

        return false;
    }
}
