# SwipeRefreshPlush
******
![效果演示](http://wx4.sinaimg.cn/mw690/9430bbffly1fequpe4nyrg208w0dc1kx.gif)
##  简介
与SwipeRereshLayout类似，不过添加了下拉加载更多的功能，下拉支持fling,不会有卡顿。

## 基本特性
* 下拉刷新
* 滑动到底部自动加载更多
* 没有数据时，可显示自定义view
* 支持AbsListView或NestChild的子类，如RecycleView,ListView  
* 自持自定义加载更多view(可参考app中extention包中的LoadMoreController实现)
  
## 使用
gradle:
   
	allprojects { 
      repositories { 
          ...			
          maven { url 'https://jitpack.io' } 
      } 
	}
	dependencies {
	        compile 'com.github.guhongya:SwipeRefreshPlush:1.0.5-a'
	}
  
设置监听  

	swipeRefreshPlush.setOnScrollListener(new SwipeRefreshPlush.OnScrollListener() {
            @Override
            public void onPullDownToRefresh() {
               ......
            }

            @Override
            public void onPullUpToRefresh() {
                  ......
               }
        }); 	
	  	   
设置数据加载完，没有更多是显示的view
````
swipeRefreshPlush.setNoMoreView(noMoreView,layoutParams);
````
  
其他：

	swipeRefreshPlush.setRefreshColorResources(new int[]{R.color.colorPrimary});//设置refresh的color
	swipeRefreshPlush.setRefresh(false);//设置是否显示refresh
	swipeRefreshPlush.showNoMore(false);//设置是否显示nomoreView,设置为true是拖动到底部将不再显示加载更多
	swipeRefreshPlush.setLoadMore(false);//设置是否显示加载更多
	swipeRefreshPlush.setScrollMode(@SwipeRefreshMode int mode)//设置模式
	swipeRefreshPlush.setLoadViewController(new LoadMoreController())//设置自定义loadMoreController

## todo
* 自定义RefreshViewController

## Licence
	    
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

   	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
