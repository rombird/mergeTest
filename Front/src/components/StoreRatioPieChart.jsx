// src/components/StoreRatioPieChart.jsx

import React from 'react';
import { Pie, PieChart, Tooltip, Cell, Legend } from 'recharts';

// 파이 조각에 사용할 색상 정의 (일반 점포, 프랜차이즈)
const COLORS = ['#00BFFF', '#40E0D0'];

/**
 * 일반 점포와 프랜차이즈 점포 비율을 보여주는 파이 차트 컴포넌트
 * @param {Array<Object>} data - {name: string, uv: number} 형식의 데이터 배열
 */
export default function StoreRatioPieChart({ data, isAnimationActive = true }) {

    // 커스텀 범례 렌더러 (3번째 이미지 스타일)
    const renderLegend = (props) => {
        const { payload } = props;
        const total = data.reduce((sum, item) => sum + item.uv, 0);
        return (
            <div style={{
                display: 'flex',
                flexDirection: 'column',
                gap: '15px',
                fontSize: '16px',
            }}>
                {payload.map((entry, index) => (
                    <div key={`legend-${index}`} style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{
                            width: '15px',
                            height: '15px',
                            borderRadius: '50%',
                            backgroundColor: entry.color
                        }} />
                        <span style={{ fontWeight: '500' }}>{entry.value}</span>
                        <span style={{ marginLeft: 'auto', fontWeight: '900' }}>{data[index].uv.toLocaleString()}개</span>
                    </div>
                ))}
            </div>
        );
    };

    // 데이터 유효성 검사: 데이터가 유효한 배열 형태가 아니면 대체 UI 표시
    if (!data || data.length < 2) {
        return (
            <div style={{
                width: '400px',
                height: '300px',
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                border: '1px solid #ddd',
                borderRadius: '8px',
                padding: '20px',
                margin: '20px 0',
                boxSizing: 'border-box'
            }}>
                <h3 style={{ color: '#555', marginBottom: '10px' }}>점포 유형별 비율 분석</h3>
                <p style={{ color: '#888' }}>선택된 지역/분기의 점포 데이터를 불러오지 못했습니다.</p>
            </div>
        );
    }

    return (
        <div style={
            { height: '300px', 
                width: '500px', 
                margin: '20px 0', 
                backgroundColor: '#f9f9f9', 
                borderRadius: '8px', 
                boxSizing: 'border-box',
                boxShadow: '0 6px 8px rgba(0, 0, 0, 0.08)'

            }}>
            <h4 style={{ fontSize: '1.1em', padding: '13px 0px 10px 11px', textAlign: 'left', fontWeight: 'bold', boxSizing: 'border-box' }}>점포수</h4>
            <PieChart width={500} height={250}>
                <Pie
                    data={data} // 부모로부터 받은 동적 데이터 사용
                    dataKey="uv" // 데이터 값 (점포 수)
                    nameKey="name" // 레이블 이름
                    cx="35%"
                    cy="48%"
                    outerRadius={95}
                    innerRadius={0}
                    isAnimationActive={isAnimationActive}
                    labelLine={false}
                >
                    {data.map((entry, index) => (
                        <Cell
                            key={`cell-${index}`}
                            fill={COLORS[index % COLORS.length]}
                        />
                    ))}
                </Pie>
                {/* Tooltip에 개수와 이름을 표시 */}
                <Tooltip
                    formatter={(value, name) => {
                        // 🚨🚨🚨 props.payload.reduce 대신, 컴포넌트의 'data' prop을 사용
                        // data는 StoreRatioPieChart 컴포넌트의 props로 전달된 전체 데이터

                        // data 배열에서 uv 값의 전체 합계를 계산
                        const total = data.reduce((sum, entry) => sum + (entry.uv || 0), 0);

                        // 현재 조각의 비율을 계산
                        const percentage = total === 0 ? 0 : ((value / total) * 100).toFixed(1);

                        // '일반 점포: 81.4%' 형태로 반환
                        return [`${name} ${percentage}%`, false];
                    }}
                />
                <Legend
                    layout="vertical"
                    align="right"
                    verticalAlign="middle"
                    iconType="circle"
                    content={renderLegend}
                    wrapperStyle={{
                        // 🚨🚨🚨 Absolute positioning을 위한 설정
                        position: 'absolute',

                        // 🚨🚨🚨 Top 50%로 수직 중앙
                        top: '50%',

                        // 🚨🚨🚨 차트 영역의 50~55% 지점부터 시작
                        left: '60%',

                        // 🚨🚨🚨 수직 중앙 정렬을 위해 자신의 높이의 절반만큼 위로 이동시키기
                        transform: 'translateY(-67%)',

                        // 필요에 따라 너비 설정 (범례 텍스트가 잘리지 않도록)
                        width: 'auto'
                    }}
                />


            </PieChart>


        </div>
    );
}