<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <title>我的商城  | 控制面板</title>
  <jsp:include page="includes/header.jsp"/>
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">
  <jsp:include page="includes/nav.jsp"/>
  <jsp:include page="includes/menu.jsp"/>
  <!-- 右侧内容块 -->
  <div class="content-wrapper">
    <!-- 标题和路径 -->
    <section class="content-header">
      <h1>用户列表<small>User Manage</small></h1>
      <ol class="breadcrumb">
        <li><a href="#"><i class="fa fa-dashboard"></i> 首页</a></li>
        <li class="active"><a href="#"><i class="fa fa-user"></i>用户管理</a></li>
        <li class="active">用户列表</li>
      </ol>
    </section>
    <!-- 内容 -->
    <section class="content container-fluid">
      <!--------------------------
        | Your Page Content Here |
        -------------------------->
    </section>
    <!-- /.content -->
  </div>
  <!-- /.content-wrapper -->
  <jsp:include page="includes/copyright.jsp"/>
</div>
<!-- ./wrapper -->
<jsp:include page="includes/footer.jsp"/>
</body>
</html>