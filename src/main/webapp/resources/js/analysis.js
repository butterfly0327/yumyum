document.addEventListener("DOMContentLoaded", () => {
  updateAuthButtons();

  const nutritionGoals = {
    calorie: 2000,
    protein: 100,
    carbohydrate: 250,
    fat: 65,
  };

  (async () => {
    try {
      await appUtils.requireLogin();
      const dietData = await appUtils.apiFetch('/api/diet', { method: 'GET', headers: { 'Content-Type': 'application/json' } });
      const today = new Date();
      const daysToSubtract = today.getDay() === 0 ? 6 : today.getDay() - 1;
      const startDate = new Date(today);
      startDate.setDate(today.getDate() - daysToSubtract);
      startDate.setHours(0, 0, 0, 0);

      const weeklyData = {};

      // 1. 각 날짜별 섭취량 합산
      dietData.forEach((entry) => {
        const entryDate = new Date(entry.date);
        entryDate.setHours(0, 0, 0, 0);
        if (entryDate >= startDate && entryDate <= today) {
          const dateKey = entry.date;
          if (!weeklyData[dateKey])
            weeklyData[dateKey] = { calorie: 0, protein: 0, carbohydrate: 0, fat: 0 };
          entry.foods.forEach((food) => {
            weeklyData[dateKey].calorie += food.energy;
            weeklyData[dateKey].protein += food.protein;
            weeklyData[dateKey].carbohydrate += food.carbohydrate;
            weeklyData[dateKey].fat += food.fat;
          });
        }
      });

      // 주간 총합
      const weeklyTotals = Object.values(weeklyData).reduce(
        (acc, daily) => {
          acc.calorie += daily.calorie;
          acc.protein += daily.protein;
          acc.carbohydrate += daily.carbohydrate;
          acc.fat += daily.fat;
          return acc;
        },
        { calorie: 0, protein: 0, carbohydrate: 0, fat: 0 }
      );

      const daysWithData = Object.keys(weeklyData).length;

      // 주간 목표
      const weeklyGoals = {
        calorie: nutritionGoals.calorie * daysWithData,
        protein: nutritionGoals.protein * daysWithData,
        carbohydrate: nutritionGoals.carbohydrate * daysWithData,
        fat: nutritionGoals.fat * daysWithData,
      };

      // 기존 차트와 카드
      updateNutritionPieChart(weeklyTotals);
      updateDailyNutritionChart(weeklyData);
      const analysisResults = generateAnalysisResults(weeklyTotals, weeklyGoals, nutritionGoals);
      updateAnalysisResults(analysisResults);

      // === 새로운 Z-score 기반 섭취량 변화 분석 ===
      const zScoreResults = generateZScoreAnalysis(weeklyData, nutritionGoals);
      updateZScoreResults(zScoreResults);
    } catch (error) {
      console.error('데이터 불러오기 오류:', error);
      document.body.innerHTML =
        '<h1 class="text-center mt-5">데이터를 불러오는 데 실패했습니다.</h1>';
    }
  })();

  // ==============================
  // 기존 분석 카드 생성 함수
  function generateAnalysisResults(weeklyTotals, weeklyGoals, dailyGoals) {
    const results = [];
    const nutrients = ["calorie", "protein", "carbohydrate", "fat"];
    const nutrientNames = {
      calorie: "총 칼로리",
      protein: "단백질",
      carbohydrate: "탄수화물",
      fat: "지방",
    };
    const checkIcon = "bi-shield-fill-check";
    const exclamationIcon = "bi-shield-fill-exclamation";
    nutrients.forEach((nutrient) => {
      const total = weeklyTotals[nutrient];
      const goal = weeklyGoals[nutrient];
      const dailyGoal = dailyGoals[nutrient];
      const diff = total - goal;
      let status = "",
        color = "",
        icon = "";
      if (diff > dailyGoal) {
        status = `이번 주 ${nutrientNames[nutrient]} 섭취가 **과한 수준**입니다.`;
        color = "danger";
        icon = exclamationIcon;
      } else if (diff < -dailyGoal) {
        status = `이번 주 ${nutrientNames[nutrient]} 섭취가 **부족한 수준**입니다.`;
        color = "warning";
        icon = exclamationIcon;
      } else {
        status = `이번 주 ${nutrientNames[nutrient]} 섭취가 **적절한 수준**입니다.`;
        color = "success";
        icon = checkIcon;
      }
      results.push({ nutrient: nutrientNames[nutrient], status, icon, color });
    });
    return results;
  }

  function updateAnalysisResults(results) {
    const container = document.getElementById("analysis-results-container");
    container.innerHTML = "";
    results.forEach((item) => {
      const col = document.createElement("div");
      col.className = "col-md-6 mb-4";
      const card = document.createElement("div");
      card.className = `card text-center p-3 h-100 border-${item.color}`;
      card.innerHTML = `
                <i class="bi ${item.icon} card-icon text-${item.color}"></i>
                <div class="card-body">
                    <h5 class="card-title fw-bold">${item.nutrient} 분석</h5>
                    <p class="card-text text-muted">${item.status}</p>
                </div>
            `;
      col.appendChild(card);
      container.appendChild(col);
    });
  }

  // ==============================
  // Z-score 기반 섭취량 변화 분석 함수
  function generateZScoreAnalysis(weeklyData, dailyGoals) {
    /* 단계별 로직
        1. 각 영양소별 데이터 집합 수집
        2. 평균과 표준편차 계산
        3. 각 데이터의 Z-score 산출
        4. 사전에 정의한 임계값과 비교
        5. 이상치 여부 판정 및 카드 생성 데이터 반환
        */

    const nutrients = ["calorie", "protein", "carbohydrate", "fat"];
    const nutrientNames = {
      calorie: "총 칼로리",
      protein: "단백질",
      carbohydrate: "탄수화물",
      fat: "지방",
    };
    const results = [];
    const checkIcon = "bi-shield-fill-check";
    const exclamationIcon = "bi-shield-fill-exclamation";

    nutrients.forEach((nutrient) => {
      // 1. 각 영양소별 데이터 배열
      const values = Object.values(weeklyData).map((d) => d[nutrient]);

      // 2. 평균과 표준편차 계산
      const mean = values.reduce((a, b) => a + b, 0) / values.length || 0;
      const std =
        Math.sqrt(values.reduce((a, b) => a + Math.pow(b - mean, 2), 0) / values.length) || 1; // std 0 방지

      // 3. Z-score 계산 (마지막 날 기준)
      const lastValue = values[values.length - 1] || 0;
      const z = Math.abs((lastValue - mean) / std);

      // 4. 임계값 비교 (5미만 정상, 5~7 잠재적 이상치, 7 이상 확실한 이상치)
      let status = "",
        color = "",
        icon = "";
      if (z >= 7) {
        status = `최근 ${nutrientNames[nutrient]} 섭취량이 **더 많이 먹음**입니다.`;
        color = "danger";
        icon = exclamationIcon;
      } else if (z >= 5) {
        status = `최근 ${nutrientNames[nutrient]} 섭취량이 **조금 더 많이 먹음** 수준입니다.`;
        color = "warning";
        icon = exclamationIcon;
      } else {
        status = `최근 ${nutrientNames[nutrient]} 섭취량이 **비슷하게 먹음** 수준입니다.`;
        color = "success";
        icon = checkIcon;
      }

      results.push({ nutrient: nutrientNames[nutrient], status, icon, color });
    });

    return results;
  }

  function updateZScoreResults(results) {
    const container = document.getElementById("analysis-results-container");

    // 구분용 헤더
    const headerRow = document.createElement("div");
    headerRow.className = "row mt-3";
    const headerCol = document.createElement("div");
    headerCol.className = "col-12";
    headerCol.innerHTML = '<h5 class="mb-3">섭취량 변화</h5>';
    headerRow.appendChild(headerCol);
    container.appendChild(headerRow);

    // 카드 4개 생성
    const row = document.createElement("div");
    row.className = "row";
    results.forEach((item) => {
      const col = document.createElement("div");
      col.className = "col-md-6 mb-4";
      const card = document.createElement("div");
      card.className = `card text-center p-3 h-100 border-${item.color}`;
      card.innerHTML = `
                <i class="bi ${item.icon} card-icon text-${item.color}"></i>
                <div class="card-body">
                    <h5 class="card-title fw-bold">${item.nutrient} 섭취량 변화</h5>
                    <p class="card-text text-muted">${item.status}</p>
                </div>
            `;
      col.appendChild(card);
      row.appendChild(col);
    });
    container.appendChild(row);
  }

  // ==============================
  // Chart.js 관련 함수
  function updateNutritionPieChart(data) {
    const ctx = document.getElementById("nutritionPieChart").getContext("2d");
    const total = data.protein + data.carbohydrate + data.fat;
    const proteinRatio = (data.protein / total) * 100;
    const carbRatio = (data.carbohydrate / total) * 100;
    const fatRatio = (data.fat / total) * 100;
    new Chart(ctx, {
      type: "pie",
      data: {
        labels: ["단백질", "탄수화물", "지방"],
        datasets: [
          {
            data: [proteinRatio.toFixed(1), carbRatio.toFixed(1), fatRatio.toFixed(1)],
            backgroundColor: ["#4BC0C0", "#FFCD56", "#FF6384"],
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "bottom" },
          tooltip: {
            callbacks: { label: (tooltipItem) => `${tooltipItem.label}: ${tooltipItem.raw} %` },
          },
        },
      },
    });
  }

  function updateDailyNutritionChart(data) {
    const ctx = document.getElementById("dailyNutritionChart").getContext("2d");
    const sortedLabels = Object.keys(data).sort();
    const proteins = sortedLabels.map((date) => data[date].protein);
    const carbohydrates = sortedLabels.map((date) => data[date].carbohydrate);
    const fats = sortedLabels.map((date) => data[date].fat);
    new Chart(ctx, {
      type: "line",
      data: {
        labels: sortedLabels,
        datasets: [
          {
            label: "단백질 (g)",
            data: proteins,
            borderColor: "#4BC0C0",
            backgroundColor: "rgba(75,192,192,0.2)",
            tension: 0.1,
          },
          {
            label: "탄수화물 (g)",
            data: carbohydrates,
            borderColor: "#FFCD56",
            backgroundColor: "rgba(255,206,86,0.2)",
            tension: 0.1,
          },
          {
            label: "지방 (g)",
            data: fats,
            borderColor: "#FF6384",
            backgroundColor: "rgba(255,99,132,0.2)",
            tension: 0.1,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: { y: { beginAtZero: true } },
        plugins: { legend: { position: "top" } },
      },
    });
  }
});
